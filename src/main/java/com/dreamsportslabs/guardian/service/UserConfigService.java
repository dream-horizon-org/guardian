package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_USER_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_ADD_PROVIDER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_AUTHENTICATE_USER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_CREATE_USER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_GET_USER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_HOST;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_PORT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_SEND_PROVIDER_DETAILS;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.UserConfigDao;
import com.dreamsportslabs.guardian.dao.model.UserConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.UpdateUserConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserConfigService {
  private final UserConfigDao userConfigDao;
  private final ChangelogService changelogService;

  public Completable createDefaultUserConfig(String tenantId) {
    UserConfigModel userConfig = buildDefaultUserConfig(tenantId);
    return userConfigDao
        .createDefaultUserConfig(userConfig)
        .andThen(
            changelogService.logConfigChange(
                tenantId, CONFIG_TYPE_USER_CONFIG, OPERATION_INSERT, null, userConfig, tenantId));
  }

  public Single<UserConfigModel> getUserConfig(String tenantId) {
    return userConfigDao
        .getUserConfig(tenantId)
        .switchIfEmpty(Single.error(USER_CONFIG_NOT_FOUND.getException()));
  }

  public Single<UserConfigModel> updateUserConfig(
      String tenantId, UpdateUserConfigRequestDto requestDto) {
    return userConfigDao
        .getUserConfig(tenantId)
        .switchIfEmpty(Single.error(USER_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              UserConfigModel updatedConfig = mergeUserConfig(tenantId, requestDto, oldConfig);
              return userConfigDao
                  .updateUserConfig(updatedConfig)
                  .andThen(getUserConfig(tenantId))
                  .flatMap(
                      newConfig ->
                          changelogService
                              .logConfigChange(
                                  tenantId,
                                  CONFIG_TYPE_USER_CONFIG,
                                  OPERATION_UPDATE,
                                  oldConfig,
                                  newConfig,
                                  tenantId)
                              .andThen(Single.just(newConfig)));
            });
  }

  private UserConfigModel mergeUserConfig(
      String tenantId, UpdateUserConfigRequestDto requestDto, UserConfigModel oldConfig) {
    return UserConfigModel.builder()
        .tenantId(tenantId)
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), oldConfig.getIsSslEnabled()))
        .host(coalesce(requestDto.getHost(), oldConfig.getHost()))
        .port(coalesce(requestDto.getPort(), oldConfig.getPort()))
        .getUserPath(coalesce(requestDto.getGetUserPath(), oldConfig.getGetUserPath()))
        .createUserPath(coalesce(requestDto.getCreateUserPath(), oldConfig.getCreateUserPath()))
        .authenticateUserPath(
            coalesce(requestDto.getAuthenticateUserPath(), oldConfig.getAuthenticateUserPath()))
        .addProviderPath(coalesce(requestDto.getAddProviderPath(), oldConfig.getAddProviderPath()))
        .sendProviderDetails(
            coalesce(requestDto.getSendProviderDetails(), oldConfig.getSendProviderDetails()))
        .build();
  }

  private UserConfigModel buildDefaultUserConfig(String tenantId) {
    return UserConfigModel.builder()
        .tenantId(tenantId)
        .isSslEnabled(DEFAULT_USER_CONFIG_IS_SSL_ENABLED)
        .host(DEFAULT_USER_CONFIG_HOST)
        .port(DEFAULT_USER_CONFIG_PORT)
        .getUserPath(DEFAULT_USER_CONFIG_GET_USER_PATH)
        .createUserPath(DEFAULT_USER_CONFIG_CREATE_USER_PATH)
        .authenticateUserPath(DEFAULT_USER_CONFIG_AUTHENTICATE_USER_PATH)
        .addProviderPath(DEFAULT_USER_CONFIG_ADD_PROVIDER_PATH)
        .sendProviderDetails(DEFAULT_USER_CONFIG_SEND_PROVIDER_DETAILS)
        .build();
  }
}
