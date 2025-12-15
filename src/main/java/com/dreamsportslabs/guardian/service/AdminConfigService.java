package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.ADMIN_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.AdminConfigDao;
import com.dreamsportslabs.guardian.dao.model.AdminConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateAdminConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateAdminConfigRequestDto;
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

  public Single<AdminConfigModel> createAdminConfig(CreateAdminConfigRequestDto requestDto) {
    AdminConfigModel adminConfig = buildAdminConfigFromCreateRequest(requestDto);
    return adminConfigDao
        .createAdminConfig(adminConfig)
        .flatMap(
            createdConfig ->
                changelogService
                    .logConfigChange(
                        createdConfig.getTenantId(),
                        CONFIG_TYPE_ADMIN_CONFIG,
                        OPERATION_INSERT,
                        null,
                        createdConfig,
                        createdConfig.getTenantId())
                    .andThen(Single.just(createdConfig)));
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
              AdminConfigModel updatedConfig = mergeAdminConfig(tenantId, requestDto, oldConfig);
              return adminConfigDao
                  .updateAdminConfig(updatedConfig)
                  .andThen(getAdminConfig(tenantId))
                  .flatMap(
                      newConfig ->
                          changelogService
                              .logConfigChange(
                                  tenantId,
                                  CONFIG_TYPE_ADMIN_CONFIG,
                                  OPERATION_UPDATE,
                                  oldConfig,
                                  newConfig,
                                  tenantId)
                              .andThen(Single.just(newConfig)));
            });
  }

  public Completable deleteAdminConfig(String tenantId) {
    return adminConfigDao
        .getAdminConfig(tenantId)
        .switchIfEmpty(Single.error(ADMIN_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                adminConfigDao
                    .deleteAdminConfig(tenantId)
                    .ignoreElement()
                    .andThen(
                        changelogService.logConfigChange(
                            tenantId,
                            CONFIG_TYPE_ADMIN_CONFIG,
                            OPERATION_DELETE,
                            oldConfig,
                            null,
                            tenantId)));
  }

  private AdminConfigModel buildAdminConfigFromCreateRequest(
      CreateAdminConfigRequestDto requestDto) {
    return AdminConfigModel.builder()
        .tenantId(requestDto.getTenantId())
        .username(requestDto.getUsername())
        .password(requestDto.getPassword())
        .build();
  }

  private AdminConfigModel mergeAdminConfig(
      String tenantId, UpdateAdminConfigRequestDto requestDto, AdminConfigModel oldConfig) {
    return AdminConfigModel.builder()
        .tenantId(tenantId)
        .username(coalesce(requestDto.getUsername(), oldConfig.getUsername()))
        .password(coalesce(requestDto.getPassword(), oldConfig.getPassword()))
        .build();
  }
}
