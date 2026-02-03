package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.CREATE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.DELETE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.GET_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcConfigQuery.UPDATE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.OidcConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateOidcConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOidcConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OidcConfigService
    extends BaseConfigService<
        OidcConfigModel, CreateOidcConfigRequestDto, UpdateOidcConfigRequestDto> {
  private final ObjectMapper objectMapper;

  @Inject
  public OidcConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      ObjectMapper objectMapper) {
    super(changelogService, mysqlClient, tenantCache);
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
  protected String getConfigType() {
    return CONFIG_TYPE_OIDC_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return OIDC_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create OIDC config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update OIDC config";
  }

  @Override
  protected OidcConfigModel mapToModel(CreateOidcConfigRequestDto requestDto) {
    return OidcConfigModel.builder()
        .issuer(requestDto.getIssuer())
        .authorizationEndpoint(requestDto.getAuthorizationEndpoint())
        .tokenEndpoint(requestDto.getTokenEndpoint())
        .userinfoEndpoint(requestDto.getUserinfoEndpoint())
        .revocationEndpoint(requestDto.getRevocationEndpoint())
        .jwksUri(requestDto.getJwksUri())
        .grantTypesSupported(requestDto.getGrantTypesSupported())
        .responseTypesSupported(requestDto.getResponseTypesSupported())
        .subjectTypesSupported(requestDto.getSubjectTypesSupported())
        .idTokenSigningAlgValuesSupported(requestDto.getIdTokenSigningAlgValuesSupported())
        .tokenEndpointAuthMethodsSupported(requestDto.getTokenEndpointAuthMethodsSupported())
        .loginPageUri(requestDto.getLoginPageUri())
        .consentPageUri(requestDto.getConsentPageUri())
        .authorizeTtl(requestDto.getAuthorizeTtl())
        .build();
  }

  @Override
  protected OidcConfigModel mergeModel(
      UpdateOidcConfigRequestDto requestDto, OidcConfigModel oldConfig) {
    return OidcConfigModel.builder()
        .issuer(coalesce(requestDto.getIssuer(), oldConfig.getIssuer()))
        .authorizationEndpoint(
            coalesce(requestDto.getAuthorizationEndpoint(), oldConfig.getAuthorizationEndpoint()))
        .tokenEndpoint(coalesce(requestDto.getTokenEndpoint(), oldConfig.getTokenEndpoint()))
        .userinfoEndpoint(
            coalesce(requestDto.getUserinfoEndpoint(), oldConfig.getUserinfoEndpoint()))
        .revocationEndpoint(
            coalesce(requestDto.getRevocationEndpoint(), oldConfig.getRevocationEndpoint()))
        .jwksUri(coalesce(requestDto.getJwksUri(), oldConfig.getJwksUri()))
        .grantTypesSupported(
            coalesce(requestDto.getGrantTypesSupported(), oldConfig.getGrantTypesSupported()))
        .responseTypesSupported(
            coalesce(requestDto.getResponseTypesSupported(), oldConfig.getResponseTypesSupported()))
        .subjectTypesSupported(
            coalesce(requestDto.getSubjectTypesSupported(), oldConfig.getSubjectTypesSupported()))
        .idTokenSigningAlgValuesSupported(
            coalesce(
                requestDto.getIdTokenSigningAlgValuesSupported(),
                oldConfig.getIdTokenSigningAlgValuesSupported()))
        .tokenEndpointAuthMethodsSupported(
            coalesce(
                requestDto.getTokenEndpointAuthMethodsSupported(),
                oldConfig.getTokenEndpointAuthMethodsSupported()))
        .loginPageUri(coalesce(requestDto.getLoginPageUri(), oldConfig.getLoginPageUri()))
        .consentPageUri(coalesce(requestDto.getConsentPageUri(), oldConfig.getConsentPageUri()))
        .authorizeTtl(coalesce(requestDto.getAuthorizeTtl(), oldConfig.getAuthorizeTtl()))
        .build();
  }

  public Single<OidcConfigModel> createOidcConfig(
      String tenantId, CreateOidcConfigRequestDto requestDto) {
    return createConfig(tenantId, requestDto);
  }

  public Single<OidcConfigModel> getOidcConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<OidcConfigModel> updateOidcConfig(
      String tenantId, UpdateOidcConfigRequestDto requestDto) {
    return updateConfig(tenantId, requestDto);
  }

  public Completable deleteOidcConfig(String tenantId) {
    return deleteConfig(tenantId);
  }
}
