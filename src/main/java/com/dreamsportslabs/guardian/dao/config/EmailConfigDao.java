package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.CREATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.DELETE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.GET_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.UPDATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.EmailConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLException;
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
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    EMAIL_CONFIG_ALREADY_EXISTS.getCustomException(
                        String.format("Email config already exists: %s", tenantId)));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<EmailConfigModel> getEmailConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_EMAIL_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result ->
                result.size() == 0
                    ? Maybe.empty()
                    : Maybe.just(JsonUtils.rowSetToList(result, EmailConfigModel.class).get(0)))
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
