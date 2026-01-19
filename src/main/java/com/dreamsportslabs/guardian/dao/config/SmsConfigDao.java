package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.CREATE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.DELETE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.GET_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.UPDATE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.SmsConfigModel;
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
public class SmsConfigDao {
  private final MysqlClient mysqlClient;
  private final ObjectMapper objectMapper;

  public Single<SmsConfigModel> createSmsConfig(
      SqlConnection client, String tenantId, SmsConfigModel smsConfig) {
    return client
        .preparedQuery(CREATE_SMS_CONFIG)
        .rxExecute(buildParams(tenantId, smsConfig))
        .map(result -> smsConfig)
        .onErrorResumeNext(
            err ->
                SqlUtils.handleMySqlError(
                    err,
                    SMS_CONFIG_ALREADY_EXISTS,
                    String.format("SMS config already exists: %s", tenantId),
                    INTERNAL_SERVER_ERROR));
  }

  public Maybe<SmsConfigModel> getSmsConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_SMS_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, SmsConfigModel.class).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateSmsConfig(
      SqlConnection client, String tenantId, SmsConfigModel smsConfig) {
    return client
        .preparedQuery(UPDATE_SMS_CONFIG)
        .rxExecute(buildParams(tenantId, smsConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteSmsConfig(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(DELETE_SMS_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, SmsConfigModel smsConfig) {
    return Tuple.tuple()
        .addValue(smsConfig.getIsSslEnabled())
        .addString(smsConfig.getHost())
        .addInteger(smsConfig.getPort())
        .addString(smsConfig.getSendSmsPath())
        .addString(smsConfig.getTemplateName())
        .addString(JsonUtils.serializeToJsonString(smsConfig.getTemplateParams(), objectMapper))
        .addString(tenantId);
  }
}
