package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.BiometricCredentialQuery.GET_CREDENTIAL_BY_ID;
import static com.dreamsportslabs.guardian.dao.query.BiometricCredentialQuery.UPDATE_SIGN_COUNT;
import static com.dreamsportslabs.guardian.dao.query.BiometricCredentialQuery.UPSERT_CREDENTIAL;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.BiometricCredentialModel;
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
public class BiometricCredentialDao {
  private final MysqlClient mysqlClient;

  public Maybe<BiometricCredentialModel> getCredential(
      String tenantId, String clientId, String userId, String credentialId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_CREDENTIAL_BY_ID)
        .rxExecute(Tuple.of(tenantId, clientId, userId, credentialId))
        .onErrorResumeNext(
            err -> {
              log.error("Failed to get biometric credential", err);
              return Single.error(INTERNAL_SERVER_ERROR.getException());
            })
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, BiometricCredentialModel.class).get(0));
  }

  public Completable updateSignCount(Long credentialId, Long signCount) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_SIGN_COUNT)
        .rxExecute(Tuple.of(signCount, credentialId))
        .onErrorResumeNext(
            err -> {
              log.error("Failed to update sign count", err);
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            })
        .ignoreElement();
  }

  public Completable upsertCredential(BiometricCredentialModel model) {
    Tuple params = Tuple.tuple();
    params.addString(model.getTenantId());
    params.addString(model.getClientId());
    params.addString(model.getUserId());
    params.addString(model.getCredentialId());
    params.addString(model.getPublicKey());
    params.addString(model.getBindingType());
    params.addInteger(model.getAlg());
    params.addLong(model.getSignCount() != null ? model.getSignCount() : 0L);
    params.addString(model.getAaguid());
    // Parameters for ON DUPLICATE KEY UPDATE (same values)
    params.addString(model.getPublicKey());
    params.addString(model.getBindingType());
    params.addInteger(model.getAlg());
    params.addLong(model.getSignCount() != null ? model.getSignCount() : 0L);
    params.addString(model.getAaguid());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPSERT_CREDENTIAL)
        .rxExecute(params)
        .onErrorResumeNext(
            err -> {
              log.error("Failed to upsert biometric credential", err);
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            })
        .ignoreElement();
  }
}
