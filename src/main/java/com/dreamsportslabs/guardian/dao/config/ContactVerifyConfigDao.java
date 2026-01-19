package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.CREATE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.DELETE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.GET_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.UPDATE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CONTACT_VERIFY_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.ContactVerifyConfigModel;
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
public class ContactVerifyConfigDao {
  private final MysqlClient mysqlClient;
  private final ObjectMapper objectMapper;

  public Single<ContactVerifyConfigModel> createContactVerifyConfig(
      SqlConnection client, String tenantId, ContactVerifyConfigModel contactVerifyConfig) {
    return client
        .preparedQuery(CREATE_CONTACT_VERIFY_CONFIG)
        .rxExecute(buildParams(tenantId, contactVerifyConfig))
        .map(result -> contactVerifyConfig)
        .onErrorResumeNext(
            err ->
                SqlUtils.handleMySqlError(
                    err,
                    CONTACT_VERIFY_CONFIG_ALREADY_EXISTS,
                    String.format("Contact verify config already exists: %s", tenantId),
                    INTERNAL_SERVER_ERROR));
  }

  public Maybe<ContactVerifyConfigModel> getContactVerifyConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_CONTACT_VERIFY_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, ContactVerifyConfigModel.class).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateContactVerifyConfig(
      SqlConnection client, String tenantId, ContactVerifyConfigModel contactVerifyConfig) {
    return client
        .preparedQuery(UPDATE_CONTACT_VERIFY_CONFIG)
        .rxExecute(buildParams(tenantId, contactVerifyConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteContactVerifyConfig(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(DELETE_CONTACT_VERIFY_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, ContactVerifyConfigModel contactVerifyConfig) {
    return Tuple.tuple()
        .addValue(contactVerifyConfig.getIsOtpMocked())
        .addValue(contactVerifyConfig.getOtpLength())
        .addValue(contactVerifyConfig.getTryLimit())
        .addValue(contactVerifyConfig.getResendLimit())
        .addValue(contactVerifyConfig.getOtpResendInterval())
        .addValue(contactVerifyConfig.getOtpValidity())
        .addString(
            JsonUtils.serializeToJsonString(
                contactVerifyConfig.getWhitelistedInputs(), objectMapper))
        .addString(tenantId);
  }
}
