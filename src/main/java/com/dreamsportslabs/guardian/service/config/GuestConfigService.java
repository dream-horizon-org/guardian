package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GUEST_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.GuestConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.GuestConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateGuestConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateGuestConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class GuestConfigService {
  private final GuestConfigDao guestConfigDao;
  private final ChangelogService changelogService;
  private final MysqlClient mysqlClient;
  private final TenantCache tenantCache;

  public Single<GuestConfigModel> createGuestConfig(
      String tenantId, CreateGuestConfigRequestDto requestDto) {
    GuestConfigModel guestConfig = mapToGuestConfigModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                guestConfigDao
                    .createGuestConfig(client, tenantId, guestConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    CONFIG_TYPE_GUEST_CONFIG,
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
        .switchIfEmpty(
            Single.error(
                INTERNAL_SERVER_ERROR.getCustomException("Failed to create guest config")));
  }

  public Single<GuestConfigModel> getGuestConfig(String tenantId) {
    return guestConfigDao
        .getGuestConfig(tenantId)
        .switchIfEmpty(Single.error(GUEST_CONFIG_NOT_FOUND.getException()));
  }

  public Single<GuestConfigModel> updateGuestConfig(
      String tenantId, UpdateGuestConfigRequestDto requestDto) {
    return guestConfigDao
        .getGuestConfig(tenantId)
        .switchIfEmpty(Single.error(GUEST_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              GuestConfigModel updatedConfig = mergeGuestConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          guestConfigDao
                              .updateGuestConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_GUEST_CONFIG,
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
                              "Failed to update guest config")));
            });
  }

  public Completable deleteGuestConfig(String tenantId) {
    return guestConfigDao
        .getGuestConfig(tenantId)
        .switchIfEmpty(Single.error(GUEST_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            guestConfigDao
                                .deleteGuestConfig(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(
                                            GUEST_CONFIG_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_GUEST_CONFIG,
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .toMaybe())
                    .doOnComplete(() -> tenantCache.invalidateCache(tenantId))
                    .ignoreElement());
  }

  private GuestConfigModel mapToGuestConfigModel(CreateGuestConfigRequestDto requestDto) {
    return GuestConfigModel.builder()
        .isEncrypted(requestDto.getIsEncrypted())
        .secretKey(requestDto.getSecretKey())
        .allowedScopes(requestDto.getAllowedScopes())
        .build();
  }

  private GuestConfigModel mergeGuestConfig(
      UpdateGuestConfigRequestDto requestDto, GuestConfigModel oldConfig) {
    return GuestConfigModel.builder()
        .isEncrypted(coalesce(requestDto.getIsEncrypted(), oldConfig.getIsEncrypted()))
        .secretKey(coalesce(requestDto.getSecretKey(), oldConfig.getSecretKey()))
        .allowedScopes(coalesce(requestDto.getAllowedScopes(), oldConfig.getAllowedScopes()))
        .build();
  }
}
