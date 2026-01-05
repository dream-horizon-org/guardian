package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.query.ContactVerifyConfigQuery.CREATE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.ContactVerifyConfigQuery.DELETE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.ContactVerifyConfigQuery.GET_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.ContactVerifyConfigQuery.UPDATE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CONTACT_VERIFY_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.ContactVerifyConfigModel;
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
public class ContactVerifyConfigDao {
  private final MysqlClient mysqlClient;

  public Single<ContactVerifyConfigModel> createContactVerifyConfig(
      ContactVerifyConfigModel contactVerifyConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_CONTACT_VERIFY_CONFIG)
        .rxExecute(buildCreateParams(contactVerifyConfig))
        .map(result -> contactVerifyConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    CONTACT_VERIFY_CONFIG_ALREADY_EXISTS.getCustomException(
                        "Contact verify config already exists: "
                            + contactVerifyConfig.getTenantId()));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<ContactVerifyConfigModel> getContactVerifyConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_CONTACT_VERIFY_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(
                  JsonUtils.rowSetToList(result, ContactVerifyConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateContactVerifyConfig(ContactVerifyConfigModel contactVerifyConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_CONTACT_VERIFY_CONFIG)
        .rxExecute(buildUpdateParams(contactVerifyConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteContactVerifyConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_CONTACT_VERIFY_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(ContactVerifyConfigModel contactVerifyConfig) {
    Tuple params = Tuple.tuple().addString(contactVerifyConfig.getTenantId());
    for (Object v : buildCommonValues(contactVerifyConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(ContactVerifyConfigModel contactVerifyConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(contactVerifyConfig)) {
      params.addValue(v);
    }
    params.addString(contactVerifyConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(ContactVerifyConfigModel contactVerifyConfig) {
    List<Object> values = new ArrayList<>();
    values.add(contactVerifyConfig.getIsOtpMocked());
    values.add(contactVerifyConfig.getOtpLength());
    values.add(contactVerifyConfig.getTryLimit());
    values.add(contactVerifyConfig.getResendLimit());
    values.add(contactVerifyConfig.getOtpResendInterval());
    values.add(contactVerifyConfig.getOtpValidity());
    values.add(contactVerifyConfig.getWhitelistedInputs());
    return values;
  }
}
