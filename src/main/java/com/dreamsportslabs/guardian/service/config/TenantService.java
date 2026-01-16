package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_TENANT;
import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_USER_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_NOT_FOUND;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.TenantDao;
import com.dreamsportslabs.guardian.dao.model.config.TenantModel;
import com.dreamsportslabs.guardian.dao.model.config.TokenConfigModel;
import com.dreamsportslabs.guardian.dao.model.config.UserConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateTenantRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTenantRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TenantService {
  private final TenantDao tenantDao;
  private final ChangelogService changelogService;
  private final UserConfigService userConfigService;
  private final TokenConfigService tokenConfigService;
  private final MysqlClient mysqlClient;
  private final TenantCache tenantCache;

  public Single<TenantModel> createTenant(CreateTenantRequestDto requestDto) {
    TenantModel tenantModel =
        TenantModel.builder().id(requestDto.getId()).name(requestDto.getName()).build();

    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                tenantDao
                    .createTenant(client, tenantModel)
                    .flatMap(
                        createdTenant -> {
                          String tenantId = createdTenant.getId();
                          UserConfigModel userConfigModel =
                              userConfigService.buildDefaultUserConfig(tenantId);
                          TokenConfigModel tokenConfigModel =
                              tokenConfigService.buildDefaultTokenConfig(tenantId);

                          return userConfigService
                              .createDefaultUserConfig(client, tenantId)
                              .andThen(
                                  tokenConfigService.createDefaultTokenConfig(client, tenantId))
                              .andThen(
                                  changelogService
                                      .logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_TENANT,
                                          OPERATION_INSERT,
                                          null,
                                          createdTenant,
                                          tenantId)
                                      .andThen(
                                          changelogService.logConfigChange(
                                              client,
                                              tenantId,
                                              CONFIG_TYPE_USER_CONFIG,
                                              OPERATION_INSERT,
                                              null,
                                              userConfigModel,
                                              tenantId))
                                      .andThen(
                                          changelogService.logConfigChange(
                                              client,
                                              tenantId,
                                              CONFIG_TYPE_TOKEN_CONFIG,
                                              OPERATION_INSERT,
                                              null,
                                              tokenConfigModel,
                                              tenantId))
                                      .andThen(Single.just(createdTenant)))
                              .doOnSuccess(tenant -> tenantCache.invalidateCache(tenant.getId()));
                        })
                    .toMaybe())
        .switchIfEmpty(
            Single.<TenantModel>error(
                INTERNAL_SERVER_ERROR.getCustomException("Failed to create tenant")));
  }

  public Single<TenantModel> getTenant(String tenantId) {
    return tenantDao
        .getTenant(tenantId)
        .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()));
  }

  public Single<TenantModel> getTenantByName(String name) {
    return tenantDao
        .getTenantByName(name)
        .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()));
  }

  public Single<TenantModel> updateTenant(String tenantId, UpdateTenantRequestDto requestDto) {
    return tenantDao
        .getTenant(tenantId)
        .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()))
        .flatMap(
            oldTenant -> {
              TenantModel updatedTenant =
                  TenantModel.builder().id(tenantId).name(requestDto.getName()).build();
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          tenantDao
                              .updateTenant(client, tenantId, requestDto.getName())
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_TENANT,
                                      OPERATION_UPDATE,
                                      oldTenant,
                                      updatedTenant,
                                      tenantId))
                              .andThen(Single.just(updatedTenant))
                              .doOnSuccess(tenant -> tenantCache.invalidateCache(tenantId))
                              .toMaybe())
                  .switchIfEmpty(
                      Single.<TenantModel>error(
                          INTERNAL_SERVER_ERROR.getCustomException("Failed to update tenant")));
            });
  }

  public Completable deleteTenant(String tenantId) {
    return tenantDao
        .getTenant(tenantId)
        .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldTenant ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            tenantDao
                                .deleteTenant(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(TENANT_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_TENANT,
                                          OPERATION_DELETE,
                                          oldTenant,
                                          null,
                                          tenantId);
                                    })
                                .doOnComplete(() -> tenantCache.invalidateCache(tenantId))
                                .toMaybe())
                    .ignoreElement());
  }
}
