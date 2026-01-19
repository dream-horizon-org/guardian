package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.CREATE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.DELETE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.GET_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.UPDATE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_PROVIDER_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.OidcProviderConfigModel;
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
public class OidcProviderConfigDao {
  private final MysqlClient mysqlClient;
  private final ObjectMapper objectMapper;

  public Single<OidcProviderConfigModel> createOidcProviderConfig(
      SqlConnection client,
      String tenantId,
      String providerName,
      OidcProviderConfigModel oidcProviderConfig) {
    return client
        .preparedQuery(CREATE_OIDC_PROVIDER_CONFIG)
        .rxExecute(buildParams(tenantId, providerName, oidcProviderConfig))
        .map(result -> oidcProviderConfig)
        .onErrorResumeNext(
            err ->
                SqlUtils.handleMySqlError(
                    err,
                    OIDC_PROVIDER_CONFIG_ALREADY_EXISTS,
                    String.format(
                        "OIDC provider config already exists: %s/%s", tenantId, providerName),
                    INTERNAL_SERVER_ERROR));
  }

  public Maybe<OidcProviderConfigModel> getOidcProviderConfig(
      String tenantId, String providerName) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_OIDC_PROVIDER_CONFIG)
        .rxExecute(Tuple.of(tenantId, providerName))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, OidcProviderConfigModel.class).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateOidcProviderConfig(
      SqlConnection client,
      String tenantId,
      String providerName,
      OidcProviderConfigModel oidcProviderConfig) {
    return client
        .preparedQuery(UPDATE_OIDC_PROVIDER_CONFIG)
        .rxExecute(buildParams(tenantId, providerName, oidcProviderConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteOidcProviderConfig(
      SqlConnection client, String tenantId, String providerName) {
    return client
        .preparedQuery(DELETE_OIDC_PROVIDER_CONFIG)
        .rxExecute(Tuple.of(tenantId, providerName))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(
      String tenantId, String providerName, OidcProviderConfigModel oidcProviderConfig) {
    return Tuple.tuple()
        .addString(oidcProviderConfig.getIssuer())
        .addString(oidcProviderConfig.getJwksUrl())
        .addString(oidcProviderConfig.getTokenUrl())
        .addString(oidcProviderConfig.getClientId())
        .addString(oidcProviderConfig.getClientSecret())
        .addString(oidcProviderConfig.getRedirectUri())
        .addString(oidcProviderConfig.getClientAuthMethod().getValue())
        .addValue(oidcProviderConfig.getIsSslEnabled())
        .addString(oidcProviderConfig.getUserIdentifier())
        .addString(
            JsonUtils.serializeToJsonString(oidcProviderConfig.getAudienceClaims(), objectMapper))
        .addString(tenantId)
        .addString(providerName);
  }
}
