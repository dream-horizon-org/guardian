package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.AUTH_CODE_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.AuthCodeConfigDao;
import com.dreamsportslabs.guardian.dao.model.AuthCodeConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateAuthCodeConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateAuthCodeConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AuthCodeConfigService {
  private final AuthCodeConfigDao authCodeConfigDao;
  private final ChangelogService changelogService;

  public Single<AuthCodeConfigModel> createAuthCodeConfig(
      CreateAuthCodeConfigRequestDto requestDto) {
    AuthCodeConfigModel authCodeConfig = buildAuthCodeConfigFromCreateRequest(requestDto);
    return authCodeConfigDao
        .createAuthCodeConfig(authCodeConfig)
        .flatMap(
            createdConfig ->
                changelogService
                    .logConfigChange(
                        createdConfig.getTenantId(),
                        CONFIG_TYPE_AUTH_CODE_CONFIG,
                        OPERATION_INSERT,
                        null,
                        createdConfig,
                        createdConfig.getTenantId())
                    .andThen(Single.just(createdConfig)));
  }

  public Single<AuthCodeConfigModel> getAuthCodeConfig(String tenantId) {
    return authCodeConfigDao
        .getAuthCodeConfig(tenantId)
        .switchIfEmpty(Single.error(AUTH_CODE_CONFIG_NOT_FOUND.getException()));
  }

  public Single<AuthCodeConfigModel> updateAuthCodeConfig(
      String tenantId, UpdateAuthCodeConfigRequestDto requestDto) {
    return authCodeConfigDao
        .getAuthCodeConfig(tenantId)
        .switchIfEmpty(Single.error(AUTH_CODE_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              AuthCodeConfigModel updatedConfig =
                  mergeAuthCodeConfig(tenantId, requestDto, oldConfig);
              return authCodeConfigDao
                  .updateAuthCodeConfig(updatedConfig)
                  .andThen(getAuthCodeConfig(tenantId))
                  .flatMap(
                      newConfig ->
                          changelogService
                              .logConfigChange(
                                  tenantId,
                                  CONFIG_TYPE_AUTH_CODE_CONFIG,
                                  OPERATION_UPDATE,
                                  oldConfig,
                                  newConfig,
                                  tenantId)
                              .andThen(Single.just(newConfig)));
            });
  }

  public Completable deleteAuthCodeConfig(String tenantId) {
    return authCodeConfigDao
        .getAuthCodeConfig(tenantId)
        .switchIfEmpty(Single.error(AUTH_CODE_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                authCodeConfigDao
                    .deleteAuthCodeConfig(tenantId)
                    .ignoreElement()
                    .andThen(
                        changelogService.logConfigChange(
                            tenantId,
                            CONFIG_TYPE_AUTH_CODE_CONFIG,
                            OPERATION_DELETE,
                            oldConfig,
                            null,
                            tenantId)));
  }

  private AuthCodeConfigModel buildAuthCodeConfigFromCreateRequest(
      CreateAuthCodeConfigRequestDto requestDto) {
    return AuthCodeConfigModel.builder()
        .tenantId(requestDto.getTenantId())
        .ttl(requestDto.getTtl())
        .length(requestDto.getLength())
        .build();
  }

  private AuthCodeConfigModel mergeAuthCodeConfig(
      String tenantId, UpdateAuthCodeConfigRequestDto requestDto, AuthCodeConfigModel oldConfig) {
    return AuthCodeConfigModel.builder()
        .tenantId(tenantId)
        .ttl(coalesce(requestDto.getTtl(), oldConfig.getTtl()))
        .length(coalesce(requestDto.getLength(), oldConfig.getLength()))
        .build();
  }
}
