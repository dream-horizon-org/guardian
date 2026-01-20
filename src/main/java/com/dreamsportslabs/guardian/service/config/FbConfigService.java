package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_FB_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_SEND_APP_SECRET;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FB_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.FbConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.FbConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateFbConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateFbConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class FbConfigService {
  private final FbConfigDao fbConfigDao;
  private final ChangelogService changelogService;
  private final MysqlClient mysqlClient;
  private final TenantCache tenantCache;

  public Single<FbConfigModel> createFbConfig(
      String tenantId, CreateFbConfigRequestDto requestDto) {
    FbConfigModel fbConfig = mapToFbConfigModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                fbConfigDao
                    .createFbConfig(client, tenantId, fbConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    CONFIG_TYPE_FB_CONFIG,
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
        .switchIfEmpty(
            Single.error(INTERNAL_SERVER_ERROR.getCustomException("Failed to create FB config")));
  }

  public Single<FbConfigModel> getFbConfig(String tenantId) {
    return fbConfigDao
        .getFbConfig(tenantId)
        .switchIfEmpty(Single.error(FB_CONFIG_NOT_FOUND.getException()));
  }

  public Single<FbConfigModel> updateFbConfig(
      String tenantId, UpdateFbConfigRequestDto requestDto) {
    return fbConfigDao
        .getFbConfig(tenantId)
        .switchIfEmpty(Single.error(FB_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              FbConfigModel updatedConfig = mergeFbConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          fbConfigDao
                              .updateFbConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_FB_CONFIG,
                                      OPERATION_UPDATE,
                                      oldConfig,
                                      updatedConfig,
                                      tenantId))
                              .andThen(Single.just(updatedConfig))
                              .toMaybe())
                  .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
                  .switchIfEmpty(
                      Single.error(
                          INTERNAL_SERVER_ERROR.getCustomException("Failed to update FB config")));
            });
  }

  public Completable deleteFbConfig(String tenantId) {
    return fbConfigDao
        .getFbConfig(tenantId)
        .switchIfEmpty(Single.error(FB_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            fbConfigDao
                                .deleteFbConfig(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(
                                            FB_CONFIG_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_FB_CONFIG,
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .toMaybe())
                    .doOnComplete(() -> tenantCache.invalidateCache(tenantId))
                    .ignoreElement());
  }

  private FbConfigModel mapToFbConfigModel(CreateFbConfigRequestDto requestDto) {
    return FbConfigModel.builder()
        .appId(requestDto.getAppId())
        .appSecret(requestDto.getAppSecret())
        .sendAppSecret(coalesce(requestDto.getSendAppSecret(), DEFAULT_SEND_APP_SECRET))
        .build();
  }

  private FbConfigModel mergeFbConfig(
      UpdateFbConfigRequestDto requestDto, FbConfigModel oldConfig) {
    return FbConfigModel.builder()
        .appId(coalesce(requestDto.getAppId(), oldConfig.getAppId()))
        .appSecret(coalesce(requestDto.getAppSecret(), oldConfig.getAppSecret()))
        .sendAppSecret(coalesce(requestDto.getSendAppSecret(), oldConfig.getSendAppSecret()))
        .build();
  }
}
