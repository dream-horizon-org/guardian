package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.ADMIN_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.AdminConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.AdminConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateAdminConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateAdminConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AdminConfigService {
  private final AdminConfigDao adminConfigDao;
  private final ChangelogService changelogService;
  private final MysqlClient mysqlClient;
  private final TenantCache tenantCache;

  public Single<AdminConfigModel> createAdminConfig(
      String tenantId, CreateAdminConfigRequestDto requestDto) {
    AdminConfigModel adminConfig = mapToAdminConfigModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                adminConfigDao
                    .createAdminConfig(client, tenantId, adminConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    CONFIG_TYPE_ADMIN_CONFIG,
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
        .switchIfEmpty(
            Single.error(
                INTERNAL_SERVER_ERROR.getCustomException("Failed to create admin config")));
  }

  public Single<AdminConfigModel> getAdminConfig(String tenantId) {
    return adminConfigDao
        .getAdminConfig(tenantId)
        .switchIfEmpty(Single.error(ADMIN_CONFIG_NOT_FOUND.getException()));
  }

  public Single<AdminConfigModel> updateAdminConfig(
      String tenantId, UpdateAdminConfigRequestDto requestDto) {
    return adminConfigDao
        .getAdminConfig(tenantId)
        .switchIfEmpty(Single.error(ADMIN_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              AdminConfigModel updatedConfig = mergeAdminConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          adminConfigDao
                              .updateAdminConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_ADMIN_CONFIG,
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
                              "Failed to update admin config")));
            });
  }

  public Completable deleteAdminConfig(String tenantId) {
    return adminConfigDao
        .getAdminConfig(tenantId)
        .switchIfEmpty(Single.error(ADMIN_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            adminConfigDao
                                .deleteAdminConfig(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(
                                            ADMIN_CONFIG_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_ADMIN_CONFIG,
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .toMaybe())
                    .doOnComplete(() -> tenantCache.invalidateCache(tenantId))
                    .ignoreElement());
  }

  private AdminConfigModel mapToAdminConfigModel(CreateAdminConfigRequestDto requestDto) {
    return AdminConfigModel.builder()
        .username(requestDto.getUsername())
        .password(requestDto.getPassword())
        .build();
  }

  private AdminConfigModel mergeAdminConfig(
      UpdateAdminConfigRequestDto requestDto, AdminConfigModel oldConfig) {
    return AdminConfigModel.builder()
        .username(coalesce(requestDto.getUsername(), oldConfig.getUsername()))
        .password(coalesce(requestDto.getPassword(), oldConfig.getPassword()))
        .build();
  }
}
