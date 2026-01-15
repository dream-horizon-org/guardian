package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.OidcConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.OidcConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateOidcConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOidcConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcConfigService {
  private final OidcConfigDao oidcConfigDao;
  private final ChangelogService changelogService;
  private final MysqlClient mysqlClient;

  public Single<OidcConfigModel> createOidcConfig(
      String tenantId, CreateOidcConfigRequestDto requestDto) {
    OidcConfigModel oidcConfig = mapToOidcConfigModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                oidcConfigDao
                    .createOidcConfig(client, tenantId, oidcConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    CONFIG_TYPE_OIDC_CONFIG,
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .switchIfEmpty(
            Single.error(INTERNAL_SERVER_ERROR.getCustomException("Failed to create OIDC config")));
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
              OidcConfigModel updatedConfig = mergeOidcConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          oidcConfigDao
                              .updateOidcConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_OIDC_CONFIG,
                                      OPERATION_UPDATE,
                                      oldConfig,
                                      updatedConfig,
                                      tenantId))
                              .andThen(Single.just(updatedConfig))
                              .toMaybe())
                  .switchIfEmpty(
                      Single.error(
                          INTERNAL_SERVER_ERROR.getCustomException(
                              "Failed to update OIDC config")));
            });
  }

  public Completable deleteOidcConfig(String tenantId) {
    return oidcConfigDao
        .getOidcConfig(tenantId)
        .switchIfEmpty(Single.error(OIDC_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            oidcConfigDao
                                .deleteOidcConfig(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(
                                            OIDC_CONFIG_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_OIDC_CONFIG,
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .toMaybe())
                    .ignoreElement());
  }

  private OidcConfigModel mapToOidcConfigModel(CreateOidcConfigRequestDto requestDto) {
    return OidcConfigModel.builder()
        .issuer(requestDto.getIssuer())
        .authorizationEndpoint(requestDto.getAuthorizationEndpoint())
        .tokenEndpoint(requestDto.getTokenEndpoint())
        .userinfoEndpoint(requestDto.getUserinfoEndpoint())
        .revocationEndpoint(requestDto.getRevocationEndpoint())
        .jwksUri(requestDto.getJwksUri())
        .grantTypesSupported(coalesce(requestDto.getGrantTypesSupported(), List.of()))
        .responseTypesSupported(coalesce(requestDto.getResponseTypesSupported(), List.of()))
        .subjectTypesSupported(coalesce(requestDto.getSubjectTypesSupported(), List.of()))
        .idTokenSigningAlgValuesSupported(
            coalesce(requestDto.getIdTokenSigningAlgValuesSupported(), List.of()))
        .tokenEndpointAuthMethodsSupported(
            coalesce(requestDto.getTokenEndpointAuthMethodsSupported(), List.of()))
        .loginPageUri(requestDto.getLoginPageUri())
        .consentPageUri(requestDto.getConsentPageUri())
        .authorizeTtl(requestDto.getAuthorizeTtl())
        .build();
  }

  private OidcConfigModel mergeOidcConfig(
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
}
