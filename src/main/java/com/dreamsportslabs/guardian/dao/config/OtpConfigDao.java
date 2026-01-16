package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.CREATE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.DELETE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.GET_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.UPDATE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OTP_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.OtpConfigModel;
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
public class OtpConfigDao {
  private final MysqlClient mysqlClient;
  private final ObjectMapper objectMapper;

  public Single<OtpConfigModel> createOtpConfig(
      SqlConnection client, String tenantId, OtpConfigModel otpConfig) {
    return client
        .preparedQuery(CREATE_OTP_CONFIG)
        .rxExecute(buildParams(tenantId, otpConfig))
        .map(result -> otpConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    OTP_CONFIG_ALREADY_EXISTS.getCustomException(
                        String.format("OTP config already exists: %s", tenantId)));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<OtpConfigModel> getOtpConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_OTP_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result ->
                result.size() == 0
                    ? Maybe.empty()
                    : Maybe.just(JsonUtils.rowSetToList(result, OtpConfigModel.class).get(0)))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateOtpConfig(
      SqlConnection client, String tenantId, OtpConfigModel otpConfig) {
    return client
        .preparedQuery(UPDATE_OTP_CONFIG)
        .rxExecute(buildParams(tenantId, otpConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteOtpConfig(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(DELETE_OTP_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, OtpConfigModel otpConfig) {
    return Tuple.tuple()
        .addValue(otpConfig.getIsOtpMocked())
        .addInteger(otpConfig.getOtpLength())
        .addInteger(otpConfig.getTryLimit())
        .addInteger(otpConfig.getResendLimit())
        .addInteger(otpConfig.getOtpResendInterval())
        .addInteger(otpConfig.getOtpValidity())
        .addInteger(otpConfig.getOtpSendWindowSeconds())
        .addInteger(otpConfig.getOtpSendWindowMaxCount())
        .addInteger(otpConfig.getOtpSendBlockSeconds())
        .addString(JsonUtils.serializeToJsonString(otpConfig.getWhitelistedInputs(), objectMapper))
        .addString(tenantId);
  }
}
