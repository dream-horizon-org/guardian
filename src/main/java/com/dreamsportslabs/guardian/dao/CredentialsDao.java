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
import io.reactivex.rxjava3.core.Single;
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
        .onErrorResumeNext(
            err -> {
              log.error("Failed to get credential", err);
              return Single.error(INTERNAL_SERVER_ERROR.getException());
            })
        .map(result -> JsonUtils.rowSetToList(result, CredentialsModel.class))
        .flatMapMaybe(
            credentials -> {
              // There will always be only one active entry per userId+deviceId pair
              // Get the first (and only) active credential if it exists
              if (credentials.isEmpty()) {
                return Maybe.empty();
              }
              CredentialsModel credential = credentials.get(0);
              if (credential.getIsActive() != null && credential.getIsActive()) {
                return Maybe.just(credential);
              }
              return Maybe.empty();
            });
  }

  public Completable insertCredential(CredentialsModel model) {
    // Revoke existing active credentials for this specific user+device pair
    // (only one active credential per device)
    Tuple revokeParams =
        Tuple.of(model.getTenantId(), model.getClientId(), model.getUserId(), model.getDeviceId());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(REVOKE_ACTIVE_CREDENTIALS_FOR_USER_DEVICE)
        .rxExecute(revokeParams)
        .onErrorResumeNext(
            err -> {
              log.error("Failed to revoke existing credentials for user device", err);
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            })
        .ignoreElement()
        .andThen(
            mysqlClient
                .getWriterPool()
                .preparedQuery(INSERT_CREDENTIAL)
                .rxExecute(
                    Tuple.tuple()
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
                        .addString(model.getAaguid()))
                .onErrorResumeNext(
                    err -> {
                      log.error("Failed to insert credential", err);
                      return Single.error(INTERNAL_SERVER_ERROR.getException(err));
                    })
                .ignoreElement());
  }
}
