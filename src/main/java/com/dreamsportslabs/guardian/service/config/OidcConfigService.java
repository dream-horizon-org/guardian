package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.BaseConfigDao;
import com.dreamsportslabs.guardian.dao.config.OidcConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.OidcConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateOidcConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOidcConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OidcConfigService
    extends BaseConfigService<
        OidcConfigModel, CreateOidcConfigRequestDto, UpdateOidcConfigRequestDto> {
  private final OidcConfigDao oidcConfigDao;

  @Inject
  public OidcConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      OidcConfigDao oidcConfigDao) {
    super(changelogService, mysqlClient, tenantCache);
    this.oidcConfigDao = oidcConfigDao;
  }

  @Override
  protected BaseConfigDao<OidcConfigModel> getDao() {
    return oidcConfigDao;
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
