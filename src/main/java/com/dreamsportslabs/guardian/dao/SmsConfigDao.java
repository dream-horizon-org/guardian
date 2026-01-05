package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.query.SmsConfigQuery.CREATE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.SmsConfigQuery.DELETE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.SmsConfigQuery.GET_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.SmsConfigQuery.UPDATE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.SmsConfigModel;
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
public class SmsConfigDao {
  private final MysqlClient mysqlClient;

  public Single<SmsConfigModel> createSmsConfig(SmsConfigModel smsConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_SMS_CONFIG)
        .rxExecute(buildCreateParams(smsConfig))
        .map(result -> smsConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    SMS_CONFIG_ALREADY_EXISTS.getCustomException(
                        "SMS config already exists: " + smsConfig.getTenantId()));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<SmsConfigModel> getSmsConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_SMS_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, SmsConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateSmsConfig(SmsConfigModel smsConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_SMS_CONFIG)
        .rxExecute(buildUpdateParams(smsConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteSmsConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_SMS_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(SmsConfigModel smsConfig) {
    Tuple params = Tuple.tuple().addString(smsConfig.getTenantId());
    for (Object v : buildCommonValues(smsConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(SmsConfigModel smsConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(smsConfig)) {
      params.addValue(v);
    }
    params.addString(smsConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(SmsConfigModel smsConfig) {
    List<Object> values = new ArrayList<>();
    values.add(smsConfig.getIsSslEnabled());
    values.add(smsConfig.getHost());
    values.add(smsConfig.getPort());
    values.add(smsConfig.getSendSmsPath());
    values.add(smsConfig.getTemplateName());
    values.add(smsConfig.getTemplateParams());
    return values;
  }
}
