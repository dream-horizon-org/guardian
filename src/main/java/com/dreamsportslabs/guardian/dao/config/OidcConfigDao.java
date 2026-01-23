package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.CREATE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.DELETE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.GET_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.UPDATE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.OidcConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.vertx.rxjava3.sqlclient.Tuple;

public class OidcConfigDao extends BaseConfigDao<OidcConfigModel> {
  private final ObjectMapper objectMapper;

  @Inject
  public OidcConfigDao(MysqlClient mysqlClient, ObjectMapper objectMapper) {
    super(mysqlClient);
    this.objectMapper = objectMapper;
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_OIDC_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_OIDC_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_OIDC_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_OIDC_CONFIG;
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return OIDC_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_OIDC_CONFIG;
  }

  @Override
  protected Class<OidcConfigModel> getModelClass() {
    return OidcConfigModel.class;
  }

  @Override
  protected Tuple buildParams(String tenantId, OidcConfigModel oidcConfig) {
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
