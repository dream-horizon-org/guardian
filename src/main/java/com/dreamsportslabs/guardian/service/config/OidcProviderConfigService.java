package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OIDC_PROVIDER_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_IDENTIFIER;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_PROVIDER_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.OidcProviderConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.OidcProviderConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateOidcProviderConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOidcProviderConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcProviderConfigService {
  private final OidcProviderConfigDao oidcProviderConfigDao;
  private final ChangelogService changelogService;
  private final MysqlClient mysqlClient;
  private final TenantCache tenantCache;

  public Single<OidcProviderConfigModel> createOidcProviderConfig(
      String tenantId, CreateOidcProviderConfigRequestDto requestDto) {
    OidcProviderConfigModel oidcProviderConfig = mapToOidcProviderConfigModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                oidcProviderConfigDao
                    .createOidcProviderConfig(
                        client, tenantId, requestDto.getProviderName(), oidcProviderConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    CONFIG_TYPE_OIDC_PROVIDER_CONFIG,
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
        .switchIfEmpty(
            Single.error(
                INTERNAL_SERVER_ERROR.getCustomException("Failed to create OIDC provider config")));
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
                  mergeOidcProviderConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          oidcProviderConfigDao
                              .updateOidcProviderConfig(
                                  client, tenantId, providerName, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_OIDC_PROVIDER_CONFIG,
                                      OPERATION_UPDATE,
                                      oldConfig,
                                      updatedConfig,
                                      tenantId))
                              .andThen(Single.just(updatedConfig))
                              .toMaybe())
                  .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
                  .switchIfEmpty(
                      Single.error(
                          INTERNAL_SERVER_ERROR.getCustomException(
                              "Failed to update OIDC provider config")));
            });
  }

  public Completable deleteOidcProviderConfig(String tenantId, String providerName) {
    return oidcProviderConfigDao
        .getOidcProviderConfig(tenantId, providerName)
        .switchIfEmpty(Single.error(OIDC_PROVIDER_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            oidcProviderConfigDao
                                .deleteOidcProviderConfig(client, tenantId, providerName)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(
                                            OIDC_PROVIDER_CONFIG_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_OIDC_PROVIDER_CONFIG,
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .toMaybe())
                    .doOnComplete(() -> tenantCache.invalidateCache(tenantId))
                    .ignoreElement());
  }

  private OidcProviderConfigModel mapToOidcProviderConfigModel(
      CreateOidcProviderConfigRequestDto requestDto) {
    return OidcProviderConfigModel.builder()
        .issuer(requestDto.getIssuer())
        .jwksUrl(requestDto.getJwksUrl())
        .tokenUrl(requestDto.getTokenUrl())
        .clientId(requestDto.getClientId())
        .clientSecret(requestDto.getClientSecret())
        .redirectUri(requestDto.getRedirectUri())
        .clientAuthMethod(requestDto.getClientAuthMethod())
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), DEFAULT_OIDC_PROVIDER_IS_SSL_ENABLED))
        .userIdentifier(coalesce(requestDto.getUserIdentifier(), DEFAULT_USER_IDENTIFIER))
        .audienceClaims(requestDto.getAudienceClaims())
        .build();
  }

  private OidcProviderConfigModel mergeOidcProviderConfig(
      UpdateOidcProviderConfigRequestDto requestDto, OidcProviderConfigModel oldConfig) {
    return OidcProviderConfigModel.builder()
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
        .audienceClaims(coalesce(requestDto.getAudienceClaims(), oldConfig.getAudienceClaims()))
        .build();
  }
}
