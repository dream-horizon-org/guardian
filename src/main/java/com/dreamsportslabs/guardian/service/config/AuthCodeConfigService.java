package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.AUTH_CODE_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.AuthCodeConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.AuthCodeConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateAuthCodeConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateAuthCodeConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
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
  private final MysqlClient mysqlClient;
  private final TenantCache tenantCache;

  public Single<AuthCodeConfigModel> createAuthCodeConfig(
      String tenantId, CreateAuthCodeConfigRequestDto requestDto) {
    AuthCodeConfigModel authCodeConfig = mapToAuthCodeConfigModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                authCodeConfigDao
                    .createAuthCodeConfig(client, tenantId, authCodeConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    CONFIG_TYPE_AUTH_CODE_CONFIG,
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
        .switchIfEmpty(
            Single.error(
                INTERNAL_SERVER_ERROR.getCustomException("Failed to create auth code config")));
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
              AuthCodeConfigModel updatedConfig = mergeAuthCodeConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          authCodeConfigDao
                              .updateAuthCodeConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_AUTH_CODE_CONFIG,
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
                              "Failed to update auth code config")));
            });
  }

  public Completable deleteAuthCodeConfig(String tenantId) {
    return authCodeConfigDao
        .getAuthCodeConfig(tenantId)
        .switchIfEmpty(Single.error(AUTH_CODE_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            authCodeConfigDao
                                .deleteAuthCodeConfig(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(
                                            AUTH_CODE_CONFIG_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_AUTH_CODE_CONFIG,
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .toMaybe())
                    .doOnComplete(() -> tenantCache.invalidateCache(tenantId))
                    .ignoreElement());
  }

  private AuthCodeConfigModel mapToAuthCodeConfigModel(CreateAuthCodeConfigRequestDto requestDto) {
    return AuthCodeConfigModel.builder()
        .ttl(requestDto.getTtl())
        .length(requestDto.getLength())
        .build();
  }

  private AuthCodeConfigModel mergeAuthCodeConfig(
      UpdateAuthCodeConfigRequestDto requestDto, AuthCodeConfigModel oldConfig) {
    return AuthCodeConfigModel.builder()
        .ttl(coalesce(requestDto.getTtl(), oldConfig.getTtl()))
        .length(coalesce(requestDto.getLength(), oldConfig.getLength()))
        .build();
  }
}
