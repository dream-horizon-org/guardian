package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GOOGLE_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.GoogleConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.GoogleConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateGoogleConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateGoogleConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
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
  private final MysqlClient mysqlClient;

  public Single<GoogleConfigModel> createGoogleConfig(
      String tenantId, CreateGoogleConfigRequestDto requestDto) {
    GoogleConfigModel googleConfig = mapToGoogleConfigModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                googleConfigDao
                    .createGoogleConfig(client, tenantId, googleConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    CONFIG_TYPE_GOOGLE_CONFIG,
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .switchIfEmpty(
            Single.error(
                INTERNAL_SERVER_ERROR.getCustomException("Failed to create Google config")));
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
              GoogleConfigModel updatedConfig = mergeGoogleConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          googleConfigDao
                              .updateGoogleConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_GOOGLE_CONFIG,
                                      OPERATION_UPDATE,
                                      oldConfig,
                                      updatedConfig,
                                      tenantId))
                              .andThen(Single.just(updatedConfig))
                              .toMaybe())
                  .switchIfEmpty(
                      Single.error(
                          INTERNAL_SERVER_ERROR.getCustomException(
                              "Failed to update Google config")));
            });
  }

  public Completable deleteGoogleConfig(String tenantId) {
    return googleConfigDao
        .getGoogleConfig(tenantId)
        .switchIfEmpty(Single.error(GOOGLE_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            googleConfigDao
                                .deleteGoogleConfig(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(
                                            GOOGLE_CONFIG_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_GOOGLE_CONFIG,
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .toMaybe())
                    .ignoreElement());
  }

  private GoogleConfigModel mapToGoogleConfigModel(CreateGoogleConfigRequestDto requestDto) {
    return GoogleConfigModel.builder()
        .clientId(requestDto.getClientId())
        .clientSecret(requestDto.getClientSecret())
        .build();
  }

  private GoogleConfigModel mergeGoogleConfig(
      UpdateGoogleConfigRequestDto requestDto, GoogleConfigModel oldConfig) {
    return GoogleConfigModel.builder()
        .clientId(coalesce(requestDto.getClientId(), oldConfig.getClientId()))
        .clientSecret(coalesce(requestDto.getClientSecret(), oldConfig.getClientSecret()))
        .build();
  }
}
