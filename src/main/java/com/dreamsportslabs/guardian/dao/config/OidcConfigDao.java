package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.CREATE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.DELETE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.GET_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.UPDATE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.OidcConfigModel;
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
public class OidcConfigDao {
  private final MysqlClient mysqlClient;
  private final ObjectMapper objectMapper;

  public Single<OidcConfigModel> createOidcConfig(
      SqlConnection client, String tenantId, OidcConfigModel oidcConfig) {
    return client
        .preparedQuery(CREATE_OIDC_CONFIG)
        .rxExecute(buildParams(tenantId, oidcConfig))
        .map(result -> oidcConfig)
        .onErrorResumeNext(
            err ->
                SqlUtils.handleMySqlError(
                    err,
                    OIDC_CONFIG_ALREADY_EXISTS,
                    String.format("OIDC config already exists: %s", tenantId),
                    INTERNAL_SERVER_ERROR));
  }

  public Maybe<OidcConfigModel> getOidcConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_OIDC_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, OidcConfigModel.class).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateOidcConfig(
      SqlConnection client, String tenantId, OidcConfigModel oidcConfig) {
    return client
        .preparedQuery(UPDATE_OIDC_CONFIG)
        .rxExecute(buildParams(tenantId, oidcConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteOidcConfig(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(DELETE_OIDC_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, OidcConfigModel oidcConfig) {
    return Tuple.tuple()
        .addString(oidcConfig.getIssuer())
        .addString(oidcConfig.getAuthorizationEndpoint())
        .addString(oidcConfig.getTokenEndpoint())
        .addString(oidcConfig.getUserinfoEndpoint())
        .addString(oidcConfig.getRevocationEndpoint())
        .addString(oidcConfig.getJwksUri())
        .addString(
            JsonUtils.serializeToJsonString(oidcConfig.getGrantTypesSupported(), objectMapper))
        .addString(
            JsonUtils.serializeToJsonString(oidcConfig.getResponseTypesSupported(), objectMapper))
        .addString(
            JsonUtils.serializeToJsonString(oidcConfig.getSubjectTypesSupported(), objectMapper))
        .addString(
            JsonUtils.serializeToJsonString(
                oidcConfig.getIdTokenSigningAlgValuesSupported(), objectMapper))
        .addString(
            JsonUtils.serializeToJsonString(
                oidcConfig.getTokenEndpointAuthMethodsSupported(), objectMapper))
        .addString(oidcConfig.getLoginPageUri())
        .addString(oidcConfig.getConsentPageUri())
        .addInteger(oidcConfig.getAuthorizeTtl())
        .addString(tenantId);
  }
}
