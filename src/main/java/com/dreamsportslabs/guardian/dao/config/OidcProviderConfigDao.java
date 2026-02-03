package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.CREATE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.DELETE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.GET_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.UPDATE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_PROVIDER_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.OidcProviderConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.dreamsportslabs.guardian.utils.SqlUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OidcProviderConfigDao extends BaseConfigDao<OidcProviderConfigModel> {
  private final ObjectMapper objectMapper;

  @Inject
  public OidcProviderConfigDao(MysqlClient mysqlClient, ObjectMapper objectMapper) {
    super(mysqlClient);
    this.objectMapper = objectMapper;
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_OIDC_PROVIDER_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_OIDC_PROVIDER_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_OIDC_PROVIDER_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_OIDC_PROVIDER_CONFIG;
  }

  @Override
  protected Tuple buildParams(String tenantId, OidcProviderConfigModel model) {
    return Tuple.tuple()
        .addString(model.getIssuer())
        .addString(model.getJwksUrl())
        .addString(model.getTokenUrl())
        .addString(model.getClientId())
        .addString(model.getClientSecret())
        .addString(model.getRedirectUri())
        .addString(model.getClientAuthMethod().getValue())
        .addValue(model.getIsSslEnabled())
        .addString(model.getUserIdentifier())
        .addString(JsonUtils.serializeToJsonString(model.getAudienceClaims(), objectMapper))
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return OIDC_PROVIDER_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_OIDC_PROVIDER_CONFIG;
  }

  @Override
  protected Class<OidcProviderConfigModel> getModelClass() {
    return OidcProviderConfigModel.class;
  }

  public Single<OidcProviderConfigModel> createConfig(
      SqlConnection client, String tenantId, String providerName, OidcProviderConfigModel model) {
    return client
        .preparedQuery(getCreateQuery())
        .rxExecute(buildParams(tenantId, providerName, model))
        .map(result -> model)
        .onErrorResumeNext(
            err ->
                SqlUtils.handleMySqlError(
                    err,
                    getDuplicateEntryError(),
                    String.format(
                        "%s: %s/%s", getDuplicateEntryMessageFormat(), tenantId, providerName),
                    INTERNAL_SERVER_ERROR));
  }

  public Maybe<OidcProviderConfigModel> getConfig(String tenantId, String providerName) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(getGetQuery())
        .rxExecute(Tuple.of(tenantId, providerName))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, getModelClass()).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateConfig(
      SqlConnection client, String tenantId, String providerName, OidcProviderConfigModel model) {
    return client
        .preparedQuery(getUpdateQuery())
        .rxExecute(buildParams(tenantId, providerName, model))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteConfig(SqlConnection client, String tenantId, String providerName) {
    return client
        .preparedQuery(getDeleteQuery())
        .rxExecute(Tuple.of(tenantId, providerName))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, String providerName, OidcProviderConfigModel model) {
    return Tuple.tuple()
        .addString(model.getIssuer())
        .addString(model.getJwksUrl())
        .addString(model.getTokenUrl())
        .addString(model.getClientId())
        .addString(model.getClientSecret())
        .addString(model.getRedirectUri())
        .addString(model.getClientAuthMethod().getValue())
        .addValue(model.getIsSslEnabled())
        .addString(model.getUserIdentifier())
        .addString(JsonUtils.serializeToJsonString(model.getAudienceClaims(), objectMapper))
        .addString(tenantId)
        .addString(providerName);
  }
}
