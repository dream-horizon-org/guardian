package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_CONFIG_NOT_FOUND;

import com.dreamsportslabs.guardian.dao.UserConfigDao;
import com.dreamsportslabs.guardian.dao.model.UserConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateUserConfigRequestDto;
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

  public Single<UserConfigModel> createUserConfig(CreateUserConfigRequestDto requestDto) {
    UserConfigModel userConfigModel =
        UserConfigModel.builder()
            .tenantId(requestDto.getTenantId())
            .isSslEnabled(requestDto.getIsSslEnabled() != null ? requestDto.getIsSslEnabled() : false)
            .host(requestDto.getHost())
            .port(requestDto.getPort())
            .getUserPath(requestDto.getGetUserPath())
            .createUserPath(requestDto.getCreateUserPath())
            .authenticateUserPath(requestDto.getAuthenticateUserPath())
            .addProviderPath(requestDto.getAddProviderPath())
            .sendProviderDetails(
                requestDto.getSendProviderDetails() != null
                    ? requestDto.getSendProviderDetails()
                    : false)
            .build();

    return userConfigDao.createUserConfig(userConfigModel);
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
            existing ->
                userConfigDao
                    .updateUserConfig(tenantId, requestDto)
                    .andThen(getUserConfig(tenantId)));
  }

  public Completable deleteUserConfig(String tenantId) {
    return userConfigDao
        .getUserConfig(tenantId)
        .switchIfEmpty(Single.error(USER_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            existing ->
                userConfigDao
                    .deleteUserConfig(tenantId)
                    .filter(deleted -> deleted)
                    .switchIfEmpty(
                        Single.error(
                            INTERNAL_SERVER_ERROR.getCustomException(
                                "Failed to delete user config")))
                    .ignoreElement());
  }
}

