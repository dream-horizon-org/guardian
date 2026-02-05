package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.CREATE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.DELETE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.GET_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OidcProviderConfigQuery.UPDATE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_PROVIDER_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_PROVIDER_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.BaseConfigDao;
import com.dreamsportslabs.guardian.dao.config.OidcProviderConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.OidcProviderConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateOidcProviderConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOidcProviderConfigRequestDto;
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
public class OidcProviderConfigService
    extends BaseConfigService<
        OidcProviderConfigModel,
        CreateOidcProviderConfigRequestDto,
        UpdateOidcProviderConfigRequestDto> {
  private final OidcProviderConfigDao oidcProviderConfigDao;
  private final ObjectMapper objectMapper;

  @Inject
  public OidcProviderConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      OidcProviderConfigDao oidcProviderConfigDao,
      ObjectMapper objectMapper) {
    super(changelogService, mysqlClient, tenantCache);
    this.oidcProviderConfigDao = oidcProviderConfigDao;
    this.objectMapper = objectMapper;
  }

  @Override
  protected BaseConfigDao<OidcProviderConfigModel> getDao() {
    return oidcProviderConfigDao;
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

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_OIDC_PROVIDER_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return OIDC_PROVIDER_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create OIDC provider config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update OIDC provider config";
  }

  @Override
  protected OidcProviderConfigModel mapToModel(CreateOidcProviderConfigRequestDto requestDto) {
    return OidcProviderConfigModel.builder()
        .issuer(requestDto.getIssuer())
        .jwksUrl(requestDto.getJwksUrl())
        .tokenUrl(requestDto.getTokenUrl())
        .clientId(requestDto.getClientId())
        .clientSecret(requestDto.getClientSecret())
        .redirectUri(requestDto.getRedirectUri())
        .clientAuthMethod(requestDto.getClientAuthMethod())
        .isSslEnabled(requestDto.getIsSslEnabled())
        .userIdentifier(requestDto.getUserIdentifier())
        .audienceClaims(requestDto.getAudienceClaims())
        .build();
  }

  @Override
  protected OidcProviderConfigModel mergeModel(
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

  public Single<OidcProviderConfigModel> createOidcProviderConfig(
      String tenantId, CreateOidcProviderConfigRequestDto requestDto, String userIdentifier) {
    OidcProviderConfigModel oidcProviderConfig = mapToModel(requestDto);
    String providerName = requestDto.getProviderName();
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                oidcProviderConfigDao
                    .createConfig(client, tenantId, providerName, oidcProviderConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    getConfigType(),
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    userIdentifier)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
        .switchIfEmpty(
            Single.error(INTERNAL_SERVER_ERROR.getCustomException(getCreateErrorMessage())));
  }

  public Single<OidcProviderConfigModel> getOidcProviderConfig(
      String tenantId, String providerName) {
    return oidcProviderConfigDao
        .getConfig(tenantId, providerName)
        .switchIfEmpty(Single.error(getNotFoundError().getException()));
  }

  public Single<OidcProviderConfigModel> updateOidcProviderConfig(
      String tenantId,
      String providerName,
      UpdateOidcProviderConfigRequestDto requestDto,
      String userIdentifier) {
    return oidcProviderConfigDao
        .getConfig(tenantId, providerName)
        .switchIfEmpty(Single.error(getNotFoundError().getException()))
        .flatMap(
            oldConfig -> {
              OidcProviderConfigModel updatedConfig = mergeModel(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          oidcProviderConfigDao
                              .updateConfig(client, tenantId, providerName, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      getConfigType(),
                                      OPERATION_UPDATE,
                                      oldConfig,
                                      updatedConfig,
                                      userIdentifier))
                              .andThen(Single.just(updatedConfig))
                              .toMaybe())
                  .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
                  .switchIfEmpty(
                      Single.error(
                          INTERNAL_SERVER_ERROR.getCustomException(getUpdateErrorMessage())));
            });
  }

  public Completable deleteOidcProviderConfig(
      String tenantId, String providerName, String userIdentifier) {
    return oidcProviderConfigDao
        .getConfig(tenantId, providerName)
        .switchIfEmpty(Single.error(getNotFoundError().getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            oidcProviderConfigDao
                                .deleteConfig(client, tenantId, providerName)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(getNotFoundError().getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          getConfigType(),
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          userIdentifier);
                                    })
                                .toMaybe())
                    .doOnComplete(() -> tenantCache.invalidateCache(tenantId))
                    .ignoreElement());
  }
}
