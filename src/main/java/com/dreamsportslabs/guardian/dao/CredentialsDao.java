package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.CredentialsQuery.GET_CREDENTIAL_BY_DEVICE_ID;
import static com.dreamsportslabs.guardian.dao.query.CredentialsQuery.INSERT_CREDENTIAL;
import static com.dreamsportslabs.guardian.dao.query.CredentialsQuery.REVOKE_ACTIVE_CREDENTIALS_FOR_USER_DEVICE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.CredentialsModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class CredentialsDao {
  private final MysqlClient mysqlClient;

  public Maybe<CredentialsModel> getCredential(
      String tenantId, String clientId, String userId, String deviceId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_CREDENTIAL_BY_DEVICE_ID)
        .rxExecute(Tuple.of(tenantId, clientId, userId, deviceId))
        .map(rs -> JsonUtils.rowSetToList(rs, CredentialsModel.class))
        // take first row if present
        .flatMapMaybe(list -> list.isEmpty() ? Maybe.empty() : Maybe.just(list.get(0)))
        // keep only active
        .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
        .onErrorResumeNext(
            err -> {
              log.error("Failed to get credential", err);
              return Maybe.error(INTERNAL_SERVER_ERROR.getException());
            });
  }

  public Completable insertCredential(CredentialsModel model) {
    // Revoke existing active credentials for this specific user+device pair
    // (only one active credential per device)
    Tuple revokeParams =
        Tuple.of(model.getTenantId(), model.getClientId(), model.getUserId(), model.getDeviceId());

    Tuple insertParams = buildInsertParams(model);

    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                client
                    .preparedQuery(REVOKE_ACTIVE_CREDENTIALS_FOR_USER_DEVICE)
                    .rxExecute(revokeParams)
                    .flatMapMaybe(
                        resp ->
                            client
                                .preparedQuery(INSERT_CREDENTIAL)
                                .rxExecute(insertParams)
                                .toMaybe()))
        .onErrorResumeNext(
            err -> {
              log.error("Failed to revoke or insert credential in transaction", err);
              return Maybe.error(INTERNAL_SERVER_ERROR.getException(err));
            })
        .ignoreElement();
  }

  private Tuple buildInsertParams(CredentialsModel model) {
    return Tuple.tuple()
        .addString(model.getTenantId())
        .addString(model.getClientId())
        .addString(model.getUserId())
        .addString(model.getDeviceId())
        .addString(model.getPlatform())
        .addString(model.getCredentialId())
        .addString(model.getPublicKey())
        .addString(model.getBindingType())
        .addInteger(model.getAlg())
        .addLong(model.getSignCount())
        .addString(model.getAaguid());
  }
}
