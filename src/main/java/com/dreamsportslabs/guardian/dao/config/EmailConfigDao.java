package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.CREATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.DELETE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.GET_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.UPDATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.EmailConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.dreamsportslabs.guardian.utils.SqlUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class EmailConfigDao {
  private final MysqlClient mysqlClient;
  private final ObjectMapper objectMapper;

  public Single<EmailConfigModel> createEmailConfig(
      SqlConnection client, String tenantId, EmailConfigModel emailConfig) {
    return client
        .preparedQuery(CREATE_EMAIL_CONFIG)
        .rxExecute(buildParams(tenantId, emailConfig))
        .map(result -> emailConfig)
        .onErrorResumeNext(
            err ->
                SqlUtils.handleMySqlError(
                    err,
                    EMAIL_CONFIG_ALREADY_EXISTS,
                    String.format("Email config already exists: %s", tenantId),
                    INTERNAL_SERVER_ERROR));
  }

  public Maybe<EmailConfigModel> getEmailConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_EMAIL_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, EmailConfigModel.class).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateEmailConfig(
      SqlConnection client, String tenantId, EmailConfigModel emailConfig) {
    return client
        .preparedQuery(UPDATE_EMAIL_CONFIG)
        .rxExecute(buildParams(tenantId, emailConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteEmailConfig(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(DELETE_EMAIL_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, EmailConfigModel emailConfig) {
    return Tuple.tuple()
        .addValue(emailConfig.getIsSslEnabled())
        .addString(emailConfig.getHost())
        .addInteger(emailConfig.getPort())
        .addString(emailConfig.getSendEmailPath())
        .addString(emailConfig.getTemplateName())
        .addString(JsonUtils.serializeToJsonString(emailConfig.getTemplateParams(), objectMapper))
        .addString(tenantId);
  }
}
