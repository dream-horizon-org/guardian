package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_CONFIG_NOT_FOUND;

import com.dreamsportslabs.guardian.dao.ChangelogDao;
import com.dreamsportslabs.guardian.dao.UserConfigDao;
import com.dreamsportslabs.guardian.dao.model.UserConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.UpdateUserConfigRequestDto;
import com.google.inject.Inject;
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
              JsonObject oldValues = JsonObject.mapFrom(oldConfig);
              UserConfigModel updatedConfig =
                  UserConfigModel.builder()
                      .tenantId(tenantId)
                      .isSslEnabled(
                          requestDto.getIsSslEnabled() != null
                              ? requestDto.getIsSslEnabled()
                              : oldConfig.getIsSslEnabled())
                      .host(
                          requestDto.getHost() != null ? requestDto.getHost() : oldConfig.getHost())
                      .port(
                          requestDto.getPort() != null ? requestDto.getPort() : oldConfig.getPort())
                      .getUserPath(
                          requestDto.getGetUserPath() != null
                              ? requestDto.getGetUserPath()
                              : oldConfig.getGetUserPath())
                      .createUserPath(
                          requestDto.getCreateUserPath() != null
                              ? requestDto.getCreateUserPath()
                              : oldConfig.getCreateUserPath())
                      .authenticateUserPath(
                          requestDto.getAuthenticateUserPath() != null
                              ? requestDto.getAuthenticateUserPath()
                              : oldConfig.getAuthenticateUserPath())
                      .addProviderPath(
                          requestDto.getAddProviderPath() != null
                              ? requestDto.getAddProviderPath()
                              : oldConfig.getAddProviderPath())
                      .sendProviderDetails(
                          requestDto.getSendProviderDetails() != null
                              ? requestDto.getSendProviderDetails()
                              : oldConfig.getSendProviderDetails())
                      .build();

              return userConfigDao
                  .updateUserConfig(updatedConfig)
                  .andThen(getUserConfig(tenantId))
                  .flatMap(
                      newConfig ->
                          changelogDao
                              .logConfigChange(
                                  tenantId,
                                  "user_config",
                                  "UPDATE",
                                  oldValues,
                                  JsonObject.mapFrom(newConfig),
                                  tenantId)
                              .andThen(Single.just(newConfig)));
            });
  }
}
