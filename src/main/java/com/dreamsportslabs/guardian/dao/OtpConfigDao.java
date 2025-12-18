package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.query.OtpConfigQuery.CREATE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.OtpConfigQuery.DELETE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.OtpConfigQuery.GET_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.OtpConfigQuery.UPDATE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OTP_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.OtpConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OtpConfigDao {
  private final MysqlClient mysqlClient;

  public Single<OtpConfigModel> createOtpConfig(OtpConfigModel otpConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_OTP_CONFIG)
        .rxExecute(buildCreateParams(otpConfig))
        .map(result -> otpConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    OTP_CONFIG_ALREADY_EXISTS.getCustomException(
                        "OTP config already exists: " + otpConfig.getTenantId()));
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
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, OtpConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateOtpConfig(OtpConfigModel otpConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_OTP_CONFIG)
        .rxExecute(buildUpdateParams(otpConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteOtpConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_OTP_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(OtpConfigModel otpConfig) {
    Tuple params = Tuple.tuple().addString(otpConfig.getTenantId());
    for (Object v : buildCommonValues(otpConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(OtpConfigModel otpConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(otpConfig)) {
      params.addValue(v);
    }
    params.addString(otpConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(OtpConfigModel otpConfig) {
    List<Object> values = new ArrayList<>();
    values.add(otpConfig.getIsOtpMocked());
    values.add(otpConfig.getOtpLength());
    values.add(otpConfig.getTryLimit());
    values.add(otpConfig.getResendLimit());
    values.add(otpConfig.getOtpResendInterval());
    values.add(otpConfig.getOtpValidity());
    values.add(otpConfig.getWhitelistedInputs());
    return values;
  }
}
