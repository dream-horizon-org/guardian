package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_USER_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_CONFIG_NOT_FOUND;

import com.dreamsportslabs.guardian.dao.ChangelogDao;
import com.dreamsportslabs.guardian.dao.UserConfigDao;
import com.dreamsportslabs.guardian.dao.model.UserConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.UpdateUserConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserConfigService {
  private final UserConfigDao userConfigDao;
  private final ChangelogDao changelogDao;

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
                          logConfigUpdate(tenantId, oldConfig, newConfig)
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

  private <T> T coalesce(T newValue, T oldValue) {
    return newValue != null ? newValue : oldValue;
  }

  private Completable logConfigUpdate(
      String tenantId, UserConfigModel oldConfig, UserConfigModel newConfig) {
    return changelogDao.logConfigChange(
        tenantId,
        CONFIG_TYPE_USER_CONFIG,
        OPERATION_UPDATE,
        JsonObject.mapFrom(oldConfig),
        JsonObject.mapFrom(newConfig),
        tenantId);
  }
}
