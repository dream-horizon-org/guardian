package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_FB_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_SEND_APP_SECRET;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FB_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.FbConfigDao;
import com.dreamsportslabs.guardian.dao.model.FbConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateFbConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateFbConfigRequestDto;
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

  public Single<FbConfigModel> createFbConfig(CreateFbConfigRequestDto requestDto) {
    FbConfigModel fbConfig = buildFbConfigFromCreateRequest(requestDto);
    return fbConfigDao
        .createFbConfig(fbConfig)
        .flatMap(
            createdConfig ->
                changelogService
                    .logConfigChange(
                        createdConfig.getTenantId(),
                        CONFIG_TYPE_FB_CONFIG,
                        OPERATION_INSERT,
                        null,
                        createdConfig,
                        createdConfig.getTenantId())
                    .andThen(Single.just(createdConfig)));
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
              FbConfigModel updatedConfig = mergeFbConfig(tenantId, requestDto, oldConfig);
              return fbConfigDao
                  .updateFbConfig(updatedConfig)
                  .andThen(getFbConfig(tenantId))
                  .flatMap(
                      newConfig ->
                          changelogService
                              .logConfigChange(
                                  tenantId,
                                  CONFIG_TYPE_FB_CONFIG,
                                  OPERATION_UPDATE,
                                  oldConfig,
                                  newConfig,
                                  tenantId)
                              .andThen(Single.just(newConfig)));
            });
  }

  public Completable deleteFbConfig(String tenantId) {
    return fbConfigDao
        .getFbConfig(tenantId)
        .switchIfEmpty(Single.error(FB_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                fbConfigDao
                    .deleteFbConfig(tenantId)
                    .ignoreElement()
                    .andThen(
                        changelogService.logConfigChange(
                            tenantId,
                            CONFIG_TYPE_FB_CONFIG,
                            OPERATION_DELETE,
                            oldConfig,
                            null,
                            tenantId)));
  }

  private FbConfigModel buildFbConfigFromCreateRequest(CreateFbConfigRequestDto requestDto) {
    return FbConfigModel.builder()
        .tenantId(requestDto.getTenantId())
        .appId(requestDto.getAppId())
        .appSecret(requestDto.getAppSecret())
        .sendAppSecret(coalesce(requestDto.getSendAppSecret(), DEFAULT_SEND_APP_SECRET))
        .build();
  }

  private FbConfigModel mergeFbConfig(
      String tenantId, UpdateFbConfigRequestDto requestDto, FbConfigModel oldConfig) {
    return FbConfigModel.builder()
        .tenantId(tenantId)
        .appId(coalesce(requestDto.getAppId(), oldConfig.getAppId()))
        .appSecret(coalesce(requestDto.getAppSecret(), oldConfig.getAppSecret()))
        .sendAppSecret(coalesce(requestDto.getSendAppSecret(), oldConfig.getSendAppSecret()))
        .build();
  }
}
