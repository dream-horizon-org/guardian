package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.OidcConfigDao;
import com.dreamsportslabs.guardian.dao.model.OidcConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateOidcConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOidcConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcConfigService {
  private final OidcConfigDao oidcConfigDao;
  private final ChangelogService changelogService;

  public Single<OidcConfigModel> createOidcConfig(CreateOidcConfigRequestDto requestDto) {
    OidcConfigModel oidcConfig = buildOidcConfigFromCreateRequest(requestDto);
    return oidcConfigDao
        .createOidcConfig(oidcConfig)
        .flatMap(
            createdConfig ->
                changelogService
                    .logConfigChange(
                        createdConfig.getTenantId(),
                        CONFIG_TYPE_OIDC_CONFIG,
                        OPERATION_INSERT,
                        null,
                        createdConfig,
                        createdConfig.getTenantId())
                    .andThen(Single.just(createdConfig)));
  }

  public Single<OidcConfigModel> getOidcConfig(String tenantId) {
    return oidcConfigDao
        .getOidcConfig(tenantId)
        .switchIfEmpty(Single.error(OIDC_CONFIG_NOT_FOUND.getException()));
  }

  public Single<OidcConfigModel> updateOidcConfig(
      String tenantId, UpdateOidcConfigRequestDto requestDto) {
    return oidcConfigDao
        .getOidcConfig(tenantId)
        .switchIfEmpty(Single.error(OIDC_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              OidcConfigModel updatedConfig = mergeOidcConfig(tenantId, requestDto, oldConfig);
              return oidcConfigDao
                  .updateOidcConfig(updatedConfig)
                  .andThen(getOidcConfig(tenantId))
                  .flatMap(
                      newConfig ->
                          changelogService
                              .logConfigChange(
                                  tenantId,
                                  CONFIG_TYPE_OIDC_CONFIG,
                                  OPERATION_UPDATE,
                                  oldConfig,
                                  newConfig,
                                  tenantId)
                              .andThen(Single.just(newConfig)));
            });
  }

  public Completable deleteOidcConfig(String tenantId) {
    return oidcConfigDao
        .getOidcConfig(tenantId)
        .switchIfEmpty(Single.error(OIDC_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                oidcConfigDao
                    .deleteOidcConfig(tenantId)
                    .ignoreElement()
                    .andThen(
                        changelogService.logConfigChange(
                            tenantId,
                            CONFIG_TYPE_OIDC_CONFIG,
                            OPERATION_DELETE,
                            oldConfig,
                            null,
                            tenantId)));
  }

  private OidcConfigModel buildOidcConfigFromCreateRequest(CreateOidcConfigRequestDto requestDto) {
    return OidcConfigModel.builder()
        .tenantId(requestDto.getTenantId())
        .issuer(requestDto.getIssuer())
        .authorizationEndpoint(requestDto.getAuthorizationEndpoint())
        .tokenEndpoint(requestDto.getTokenEndpoint())
        .userinfoEndpoint(requestDto.getUserinfoEndpoint())
        .revocationEndpoint(requestDto.getRevocationEndpoint())
        .jwksUri(requestDto.getJwksUri())
        .grantTypesSupported(encodeJsonArray(requestDto.getGrantTypesSupported()))
        .responseTypesSupported(encodeJsonArray(requestDto.getResponseTypesSupported()))
        .subjectTypesSupported(encodeJsonArray(requestDto.getSubjectTypesSupported()))
        .idTokenSigningAlgValuesSupported(
            encodeJsonArray(requestDto.getIdTokenSigningAlgValuesSupported()))
        .tokenEndpointAuthMethodsSupported(
            encodeJsonArray(requestDto.getTokenEndpointAuthMethodsSupported()))
        .loginPageUri(requestDto.getLoginPageUri())
        .consentPageUri(requestDto.getConsentPageUri())
        .authorizeTtl(requestDto.getAuthorizeTtl())
        .build();
  }

  private OidcConfigModel mergeOidcConfig(
      String tenantId, UpdateOidcConfigRequestDto requestDto, OidcConfigModel oldConfig) {
    return OidcConfigModel.builder()
        .tenantId(tenantId)
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
            requestDto.getGrantTypesSupported() != null
                ? encodeJsonArray(requestDto.getGrantTypesSupported())
                : oldConfig.getGrantTypesSupported())
        .responseTypesSupported(
            requestDto.getResponseTypesSupported() != null
                ? encodeJsonArray(requestDto.getResponseTypesSupported())
                : oldConfig.getResponseTypesSupported())
        .subjectTypesSupported(
            requestDto.getSubjectTypesSupported() != null
                ? encodeJsonArray(requestDto.getSubjectTypesSupported())
                : oldConfig.getSubjectTypesSupported())
        .idTokenSigningAlgValuesSupported(
            requestDto.getIdTokenSigningAlgValuesSupported() != null
                ? encodeJsonArray(requestDto.getIdTokenSigningAlgValuesSupported())
                : oldConfig.getIdTokenSigningAlgValuesSupported())
        .tokenEndpointAuthMethodsSupported(
            requestDto.getTokenEndpointAuthMethodsSupported() != null
                ? encodeJsonArray(requestDto.getTokenEndpointAuthMethodsSupported())
                : oldConfig.getTokenEndpointAuthMethodsSupported())
        .loginPageUri(coalesce(requestDto.getLoginPageUri(), oldConfig.getLoginPageUri()))
        .consentPageUri(coalesce(requestDto.getConsentPageUri(), oldConfig.getConsentPageUri()))
        .authorizeTtl(coalesce(requestDto.getAuthorizeTtl(), oldConfig.getAuthorizeTtl()))
        .build();
  }

  private String encodeJsonArray(List<String> list) {
    if (list == null) {
      return "[]";
    }
    return new JsonArray(list).encode();
  }
}
