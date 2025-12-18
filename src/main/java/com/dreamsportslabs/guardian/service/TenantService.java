package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_TENANT;
import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_USER_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_NOT_FOUND;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.TenantDao;
import com.dreamsportslabs.guardian.dao.model.TenantModel;
import com.dreamsportslabs.guardian.dao.model.TokenConfigModel;
import com.dreamsportslabs.guardian.dao.model.UserConfigModel;
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
  private final MysqlClient mysqlClient;

  public Single<TenantModel> createTenant(CreateTenantRequestDto requestDto) {
    TenantModel tenantModel =
        TenantModel.builder().id(requestDto.getId()).name(requestDto.getName()).build();

    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                tenantDao
                    .createTenantInTransaction(client, tenantModel)
                    .flatMap(
                        createdTenant -> {
                          String tenantId = createdTenant.getId();
                          UserConfigModel userConfig =
                              userConfigService.buildDefaultUserConfig(tenantId);
                          TokenConfigModel tokenConfig =
                              tokenConfigService.buildDefaultTokenConfig(tenantId);

                          return userConfigService
                              .createDefaultUserConfigInTransaction(client, tenantId)
                              .andThen(
                                  tokenConfigService.createDefaultTokenConfigInTransaction(
                                      client, tenantId))
                              .andThen(
                                  Single.just(
                                      new TenantCreationResult(
                                          createdTenant, userConfig, tokenConfig)));
                        })
                    .toMaybe())
        .switchIfEmpty(
            Single.<TenantCreationResult>error(
                new IllegalStateException("Failed to create tenant")))
        .flatMap(
            (TenantCreationResult result) -> {
              String tenantId = result.tenant().getId();
              return changelogService
                  .logConfigChange(
                      tenantId,
                      CONFIG_TYPE_TENANT,
                      OPERATION_INSERT,
                      null,
                      result.tenant(),
                      tenantId)
                  .andThen(
                      changelogService.logConfigChange(
                          tenantId,
                          CONFIG_TYPE_USER_CONFIG,
                          OPERATION_INSERT,
                          null,
                          result.userConfig(),
                          tenantId))
                  .andThen(
                      changelogService.logConfigChange(
                          tenantId,
                          CONFIG_TYPE_TOKEN_CONFIG,
                          OPERATION_INSERT,
                          null,
                          result.tokenConfig(),
                          tenantId))
                  .andThen(Single.just(result.tenant()));
            });
  }

  private record TenantCreationResult(
      TenantModel tenant, UserConfigModel userConfig, TokenConfigModel tokenConfig) {}

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
              return tenantDao
                  .updateTenant(tenantId, requestDto.getName())
                  .andThen(
                      changelogService
                          .logConfigChange(
                              tenantId,
                              CONFIG_TYPE_TENANT,
                              OPERATION_UPDATE,
                              oldTenant,
                              updatedTenant,
                              tenantId)
                          .andThen(Single.just(updatedTenant)));
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
                    .flatMapCompletable(
                        deleted -> {
                          if (!deleted) {
                            return Completable.error(TENANT_NOT_FOUND.getException());
                          }
                          return changelogService.logConfigChange(
                              tenantId,
                              CONFIG_TYPE_TENANT,
                              OPERATION_DELETE,
                              oldTenant,
                              null,
                              tenantId);
                        }));
  }
}
