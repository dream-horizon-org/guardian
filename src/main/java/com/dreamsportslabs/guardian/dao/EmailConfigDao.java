package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.EmailConfigQuery.CREATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.EmailConfigQuery.DELETE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.EmailConfigQuery.GET_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.EmailConfigQuery.UPDATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.JsonUtils.serializeToJsonString;
import static com.dreamsportslabs.guardian.utils.SqlUtils.prepareUpdateQuery;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.EmailConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.UpdateEmailConfigRequestDto;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class EmailConfigDao {
  private final MysqlClient mysqlClient;
  private final ObjectMapper objectMapper;

  public Single<EmailConfigModel> createEmailConfig(EmailConfigModel emailConfig) {
    Tuple params =
        Tuple.tuple()
            .addString(emailConfig.getTenantId())
            .addBoolean(
                emailConfig.getIsSslEnabled() != null ? emailConfig.getIsSslEnabled() : false)
            .addString(emailConfig.getHost())
            .addInteger(emailConfig.getPort() != null ? emailConfig.getPort() : 80)
            .addString(emailConfig.getSendEmailPath())
            .addString(emailConfig.getTemplateName())
            .addString(serializeToJsonString(emailConfig.getTemplateParams(), objectMapper));
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_EMAIL_CONFIG)
        .rxExecute(params)
        .map(result -> emailConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException) {
                int errorCode = mySQLException.getErrorCode();
                if (errorCode == 1062) {
                  return Single.error(
                      EMAIL_CONFIG_ALREADY_EXISTS.getCustomException(
                          "Email config already exists for tenant: " + emailConfig.getTenantId()));
                }
                if (errorCode == 1452) {
                  return Single.error(
                      TENANT_NOT_FOUND.getCustomException(
                          "Tenant not found: " + emailConfig.getTenantId()));
                }
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
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, EmailConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateEmailConfig(String tenantId, UpdateEmailConfigRequestDto updateRequest) {
    Pair<String, Tuple> queryAndTuple = prepareUpdateQuery(updateRequest);
    Tuple tuple = queryAndTuple.getRight().addString(tenantId);
    String query = UPDATE_EMAIL_CONFIG.replace("<<insert_attributes>>", queryAndTuple.getLeft());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(query)
        .rxExecute(tuple)
        .ignoreElement()
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == 1062) {
                return Completable.error(
                    EMAIL_CONFIG_ALREADY_EXISTS.getCustomException(
                        "Email config already exists for tenant"));
              }

              return Completable.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Single<Boolean> deleteEmailConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_EMAIL_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}
