package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_IDENTIFIER;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_PROVIDER_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.OidcProviderConfigDao;
import com.dreamsportslabs.guardian.dao.model.OidcProviderConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateOidcProviderConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOidcProviderConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcProviderConfigService {
  private final OidcProviderConfigDao oidcProviderConfigDao;
  private final ChangelogService changelogService;

  public Single<OidcProviderConfigModel> createOidcProviderConfig(
      CreateOidcProviderConfigRequestDto requestDto) {
    OidcProviderConfigModel oidcProviderConfig =
        buildOidcProviderConfigFromCreateRequest(requestDto);
    return oidcProviderConfigDao
        .createOidcProviderConfig(oidcProviderConfig)
        .flatMap(
            createdConfig ->
                changelogService
                    .logConfigChange(
                        createdConfig.getTenantId(),
                        CONFIG_TYPE_OIDC_PROVIDER_CONFIG,
                        OPERATION_INSERT,
                        null,
                        createdConfig,
                        createdConfig.getTenantId())
                    .andThen(Single.just(createdConfig)));
  }

  public Single<OidcProviderConfigModel> getOidcProviderConfig(
      String tenantId, String providerName) {
    return oidcProviderConfigDao
        .getOidcProviderConfig(tenantId, providerName)
        .switchIfEmpty(Single.error(OIDC_PROVIDER_CONFIG_NOT_FOUND.getException()));
  }

  public Single<OidcProviderConfigModel> updateOidcProviderConfig(
      String tenantId, String providerName, UpdateOidcProviderConfigRequestDto requestDto) {
    return oidcProviderConfigDao
        .getOidcProviderConfig(tenantId, providerName)
        .switchIfEmpty(Single.error(OIDC_PROVIDER_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              OidcProviderConfigModel updatedConfig =
                  mergeOidcProviderConfig(tenantId, providerName, requestDto, oldConfig);
              return oidcProviderConfigDao
                  .updateOidcProviderConfig(updatedConfig)
                  .andThen(
                      changelogService
                          .logConfigChange(
                              tenantId,
                              CONFIG_TYPE_OIDC_PROVIDER_CONFIG,
                              OPERATION_UPDATE,
                              oldConfig,
                              updatedConfig,
                              tenantId)
                          .andThen(Single.just(updatedConfig)));
            });
  }

  public Completable deleteOidcProviderConfig(String tenantId, String providerName) {
    return oidcProviderConfigDao
        .getOidcProviderConfig(tenantId, providerName)
        .switchIfEmpty(Single.error(OIDC_PROVIDER_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                oidcProviderConfigDao
                    .deleteOidcProviderConfig(tenantId, providerName)
                    .flatMapCompletable(
                        deleted -> {
                          if (!deleted) {
                            return Completable.error(OIDC_PROVIDER_CONFIG_NOT_FOUND.getException());
                          }
                          return changelogService.logConfigChange(
                              tenantId,
                              CONFIG_TYPE_OIDC_PROVIDER_CONFIG,
                              OPERATION_DELETE,
                              oldConfig,
                              null,
                              tenantId);
                        }));
  }

  private OidcProviderConfigModel buildOidcProviderConfigFromCreateRequest(
      CreateOidcProviderConfigRequestDto requestDto) {
    return OidcProviderConfigModel.builder()
        .tenantId(requestDto.getTenantId())
        .providerName(requestDto.getProviderName())
        .issuer(requestDto.getIssuer())
        .jwksUrl(requestDto.getJwksUrl())
        .tokenUrl(requestDto.getTokenUrl())
        .clientId(requestDto.getClientId())
        .clientSecret(requestDto.getClientSecret())
        .redirectUri(requestDto.getRedirectUri())
        .clientAuthMethod(requestDto.getClientAuthMethod())
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), DEFAULT_IS_SSL_ENABLED))
        .userIdentifier(coalesce(requestDto.getUserIdentifier(), DEFAULT_USER_IDENTIFIER))
        .audienceClaims(encodeAudienceClaims(requestDto.getAudienceClaims()))
        .build();
  }

  private OidcProviderConfigModel mergeOidcProviderConfig(
      String tenantId,
      String providerName,
      UpdateOidcProviderConfigRequestDto requestDto,
      OidcProviderConfigModel oldConfig) {
    return OidcProviderConfigModel.builder()
        .tenantId(tenantId)
        .providerName(providerName)
        .issuer(coalesce(requestDto.getIssuer(), oldConfig.getIssuer()))
        .jwksUrl(coalesce(requestDto.getJwksUrl(), oldConfig.getJwksUrl()))
        .tokenUrl(coalesce(requestDto.getTokenUrl(), oldConfig.getTokenUrl()))
        .clientId(coalesce(requestDto.getClientId(), oldConfig.getClientId()))
        .clientSecret(coalesce(requestDto.getClientSecret(), oldConfig.getClientSecret()))
        .redirectUri(coalesce(requestDto.getRedirectUri(), oldConfig.getRedirectUri()))
        .clientAuthMethod(
            coalesce(requestDto.getClientAuthMethod(), oldConfig.getClientAuthMethod()))
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), oldConfig.getIsSslEnabled()))
        .userIdentifier(coalesce(requestDto.getUserIdentifier(), oldConfig.getUserIdentifier()))
        .audienceClaims(
            requestDto.getAudienceClaims() != null
                ? encodeAudienceClaims(requestDto.getAudienceClaims())
                : oldConfig.getAudienceClaims())
        .build();
  }

  private String encodeAudienceClaims(java.util.List<String> audienceClaims) {
    return new JsonArray(audienceClaims).encode();
  }
}
