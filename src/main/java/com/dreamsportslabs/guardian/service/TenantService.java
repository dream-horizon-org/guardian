package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_TENANT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_NOT_FOUND;

import com.dreamsportslabs.guardian.dao.TenantDao;
import com.dreamsportslabs.guardian.dao.model.TenantModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateTenantRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTenantRequestDto;
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

  public Single<TenantModel> createTenant(CreateTenantRequestDto requestDto) {
    TenantModel tenantModel =
        TenantModel.builder().id(requestDto.getId()).name(requestDto.getName()).build();

    return tenantDao
        .createTenant(tenantModel)
        .flatMap(
            createdTenant ->
                changelogService
                    .logConfigChange(
                        createdTenant.getId(),
                        CONFIG_TYPE_TENANT,
                        OPERATION_INSERT,
                        null,
                        createdTenant,
                        createdTenant.getId())
                    .andThen(createMandatoryConfigs(createdTenant.getId()))
                    .andThen(Single.just(createdTenant)));
  }

  private Completable createMandatoryConfigs(String tenantId) {
    return userConfigService
        .createDefaultUserConfig(tenantId)
        .andThen(tokenConfigService.createDefaultTokenConfig(tenantId));
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
            oldTenant ->
                tenantDao
                    .updateTenant(tenantId, requestDto.getName())
                    .andThen(getTenant(tenantId))
                    .flatMap(
                        newTenant ->
                            changelogService
                                .logConfigChange(
                                    tenantId,
                                    CONFIG_TYPE_TENANT,
                                    OPERATION_UPDATE,
                                    oldTenant,
                                    newTenant,
                                    tenantId)
                                .andThen(Single.just(newTenant))));
  }

  public Completable deleteTenant(String tenantId) {
    return tenantDao
        .getTenant(tenantId)
        .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()))
        .flatMapCompletable(oldTenant -> tenantDao.deleteTenant(tenantId).ignoreElement());
  }
}
