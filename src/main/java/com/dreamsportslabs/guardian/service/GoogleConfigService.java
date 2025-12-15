package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GOOGLE_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.GoogleConfigDao;
import com.dreamsportslabs.guardian.dao.model.GoogleConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateGoogleConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateGoogleConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class GoogleConfigService {
  private final GoogleConfigDao googleConfigDao;
  private final ChangelogService changelogService;

  public Single<GoogleConfigModel> createGoogleConfig(CreateGoogleConfigRequestDto requestDto) {
    GoogleConfigModel googleConfig = buildGoogleConfigFromCreateRequest(requestDto);
    return googleConfigDao
        .createGoogleConfig(googleConfig)
        .flatMap(
            createdConfig ->
                changelogService
                    .logConfigChange(
                        createdConfig.getTenantId(),
                        CONFIG_TYPE_GOOGLE_CONFIG,
                        OPERATION_INSERT,
                        null,
                        createdConfig,
                        createdConfig.getTenantId())
                    .andThen(Single.just(createdConfig)));
  }

  public Single<GoogleConfigModel> getGoogleConfig(String tenantId) {
    return googleConfigDao
        .getGoogleConfig(tenantId)
        .switchIfEmpty(Single.error(GOOGLE_CONFIG_NOT_FOUND.getException()));
  }

  public Single<GoogleConfigModel> updateGoogleConfig(
      String tenantId, UpdateGoogleConfigRequestDto requestDto) {
    return googleConfigDao
        .getGoogleConfig(tenantId)
        .switchIfEmpty(Single.error(GOOGLE_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              GoogleConfigModel updatedConfig = mergeGoogleConfig(tenantId, requestDto, oldConfig);
              return googleConfigDao
                  .updateGoogleConfig(updatedConfig)
                  .andThen(getGoogleConfig(tenantId))
                  .flatMap(
                      newConfig ->
                          changelogService
                              .logConfigChange(
                                  tenantId,
                                  CONFIG_TYPE_GOOGLE_CONFIG,
                                  OPERATION_UPDATE,
                                  oldConfig,
                                  newConfig,
                                  tenantId)
                              .andThen(Single.just(newConfig)));
            });
  }

  public Completable deleteGoogleConfig(String tenantId) {
    return googleConfigDao
        .getGoogleConfig(tenantId)
        .switchIfEmpty(Single.error(GOOGLE_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                googleConfigDao
                    .deleteGoogleConfig(tenantId)
                    .ignoreElement()
                    .andThen(
                        changelogService.logConfigChange(
                            tenantId,
                            CONFIG_TYPE_GOOGLE_CONFIG,
                            OPERATION_DELETE,
                            oldConfig,
                            null,
                            tenantId)));
  }

  private GoogleConfigModel buildGoogleConfigFromCreateRequest(
      CreateGoogleConfigRequestDto requestDto) {
    return GoogleConfigModel.builder()
        .tenantId(requestDto.getTenantId())
        .clientId(requestDto.getClientId())
        .clientSecret(requestDto.getClientSecret())
        .build();
  }

  private GoogleConfigModel mergeGoogleConfig(
      String tenantId, UpdateGoogleConfigRequestDto requestDto, GoogleConfigModel oldConfig) {
    return GoogleConfigModel.builder()
        .tenantId(tenantId)
        .clientId(coalesce(requestDto.getClientId(), oldConfig.getClientId()))
        .clientSecret(coalesce(requestDto.getClientSecret(), oldConfig.getClientSecret()))
        .build();
  }
}
