package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_TENANT;
import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_USER_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_TENANT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.dao.config.query.TenantQuery.CREATE_TENANT;
import static com.dreamsportslabs.guardian.dao.config.query.TenantQuery.DELETE_TENANT;
import static com.dreamsportslabs.guardian.dao.config.query.TenantQuery.GET_TENANT;
import static com.dreamsportslabs.guardian.dao.config.query.TenantQuery.UPDATE_TENANT;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_NOT_FOUND;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.BaseConfigDao;
import com.dreamsportslabs.guardian.dao.config.TenantDao;
import com.dreamsportslabs.guardian.dao.model.config.TenantModel;
import com.dreamsportslabs.guardian.dao.model.config.TokenConfigModel;
import com.dreamsportslabs.guardian.dao.model.config.UserConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateTenantRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTenantRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantService
    extends BaseConfigService<TenantModel, CreateTenantRequestDto, UpdateTenantRequestDto> {
  private final TenantDao tenantDao;
  private final UserConfigService userConfigService;
  private final TokenConfigService tokenConfigService;

  @Inject
  public TenantService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      TenantDao tenantDao,
      UserConfigService userConfigService,
      TokenConfigService tokenConfigService) {
    super(changelogService, mysqlClient, tenantCache);
    this.tenantDao = tenantDao;
    this.userConfigService = userConfigService;
    this.tokenConfigService = tokenConfigService;
  }

  @Override
  protected BaseConfigDao<TenantModel> getDao() {
    return tenantDao;
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_TENANT;
  }

  @Override
  protected String getGetQuery() {
    return GET_TENANT;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_TENANT;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_TENANT;
  }

  @Override
  protected Tuple buildParams(String tenantId, TenantModel model) {
    return Tuple.tuple().addString(model.getId()).addString(model.getName());
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return TENANT_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_TENANT_ID;
  }

  @Override
  protected Class<TenantModel> getModelClass() {
    return TenantModel.class;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_TENANT;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return TENANT_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create tenant";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update tenant";
  }

  @Override
  protected TenantModel mapToModel(CreateTenantRequestDto requestDto) {
    return TenantModel.builder().id(requestDto.getId()).name(requestDto.getName()).build();
  }

  @Override
  protected TenantModel mergeModel(UpdateTenantRequestDto requestDto, TenantModel oldConfig) {
    return TenantModel.builder().id(oldConfig.getId()).name(requestDto.getName()).build();
  }

  @Override
  public Single<TenantModel> createConfig(
      String tenantId, CreateTenantRequestDto requestDto, String userIdentifier) {
    TenantModel tenantModel = mapToModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                getDao()
                    .createConfig(client, tenantId, tenantModel)
                    .flatMap(
                        createdTenant -> {
                          String createdTenantId = createdTenant.getId();
                          UserConfigModel userConfigModel =
                              userConfigService.buildDefaultUserConfig(createdTenantId);
                          TokenConfigModel tokenConfigModel =
                              tokenConfigService.buildDefaultTokenConfig(createdTenantId);

                          return userConfigService
                              .createDefaultUserConfig(client, createdTenantId)
                              .andThen(
                                  tokenConfigService.createDefaultTokenConfig(
                                      client, createdTenantId))
                              .andThen(
                                  changelogService
                                      .logConfigChange(
                                          client,
                                          createdTenantId,
                                          CONFIG_TYPE_TENANT,
                                          OPERATION_INSERT,
                                          null,
                                          createdTenant,
                                          userIdentifier)
                                      .andThen(
                                          changelogService.logConfigChange(
                                              client,
                                              createdTenantId,
                                              CONFIG_TYPE_USER_CONFIG,
                                              OPERATION_INSERT,
                                              null,
                                              userConfigModel,
                                              userIdentifier))
                                      .andThen(
                                          changelogService.logConfigChange(
                                              client,
                                              createdTenantId,
                                              CONFIG_TYPE_TOKEN_CONFIG,
                                              OPERATION_INSERT,
                                              null,
                                              tokenConfigModel,
                                              userIdentifier))
                                      .andThen(Single.just(createdTenant)));
                        })
                    .toMaybe())
        .doOnSuccess(tenant -> tenantCache.invalidateCache(tenant.getId()))
        .switchIfEmpty(
            Single.error(INTERNAL_SERVER_ERROR.getCustomException(getCreateErrorMessage())));
  }

  public Single<TenantModel> createTenant(
      CreateTenantRequestDto requestDto, String userIdentifier) {
    return createConfig(requestDto.getId(), requestDto, userIdentifier);
  }

  public Single<TenantModel> getTenant(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<TenantModel> getTenantByName(String name) {
    return tenantDao
        .getTenantByName(name)
        .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()));
  }

  public Single<TenantModel> updateTenant(
      String tenantId, UpdateTenantRequestDto requestDto, String userIdentifier) {
    return updateConfig(tenantId, requestDto, userIdentifier);
  }

  public Completable deleteTenant(String tenantId, String userIdentifier) {
    return deleteConfig(tenantId, userIdentifier);
  }
}
