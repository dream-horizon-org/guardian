package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_NOT_FOUND;

import com.dreamsportslabs.guardian.dao.ChangelogDao;
import com.dreamsportslabs.guardian.dao.TenantDao;
import com.dreamsportslabs.guardian.dao.model.TenantModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateTenantRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTenantRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TenantService {
  private final TenantDao tenantDao;
  private final ChangelogDao changelogDao;

  public Single<TenantModel> createTenant(CreateTenantRequestDto requestDto) {
    TenantModel tenantModel =
        TenantModel.builder().id(requestDto.getId()).name(requestDto.getName()).build();

    return tenantDao
        .createTenant(tenantModel)
        .flatMap(
            createdTenant ->
                changelogDao
                    .logConfigChange(
                        createdTenant.getId(),
                        "tenant",
                        "INSERT",
                        null,
                        JsonObject.mapFrom(createdTenant),
                        createdTenant.getId())
                    .andThen(Single.just(createdTenant)));
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
              JsonObject oldValues = JsonObject.mapFrom(oldTenant);
              return tenantDao
                  .updateTenant(tenantId, requestDto.getName())
                  .andThen(getTenant(tenantId))
                  .flatMap(
                      newTenant ->
                          changelogDao
                              .logConfigChange(
                                  tenantId,
                                  "tenant",
                                  "UPDATE",
                                  oldValues,
                                  JsonObject.mapFrom(newTenant),
                                  tenantId)
                              .andThen(Single.just(newTenant)));
            });
  }

  public Completable deleteTenant(String tenantId) {
    return tenantDao
        .getTenant(tenantId)
        .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldTenant ->
                tenantDao
                    .deleteTenant(tenantId)
                    .filter(deleted -> deleted)
                    .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()))
                    .ignoreElement());
  }
}
