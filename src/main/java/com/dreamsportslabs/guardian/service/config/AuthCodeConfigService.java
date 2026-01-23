package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.AUTH_CODE_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.AuthCodeConfigDao;
import com.dreamsportslabs.guardian.dao.config.BaseConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.AuthCodeConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateAuthCodeConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateAuthCodeConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthCodeConfigService
    extends BaseConfigService<
        AuthCodeConfigModel, CreateAuthCodeConfigRequestDto, UpdateAuthCodeConfigRequestDto> {
  private final AuthCodeConfigDao authCodeConfigDao;

  @Inject
  public AuthCodeConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      AuthCodeConfigDao authCodeConfigDao) {
    super(changelogService, mysqlClient, tenantCache);
    this.authCodeConfigDao = authCodeConfigDao;
  }

  @Override
  protected BaseConfigDao<AuthCodeConfigModel> getDao() {
    return authCodeConfigDao;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_AUTH_CODE_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return AUTH_CODE_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create auth code config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update auth code config";
  }

  @Override
  protected AuthCodeConfigModel mapToModel(CreateAuthCodeConfigRequestDto requestDto) {
    return AuthCodeConfigModel.builder()
        .ttl(requestDto.getTtl())
        .length(requestDto.getLength())
        .build();
  }

  @Override
  protected AuthCodeConfigModel mergeModel(
      UpdateAuthCodeConfigRequestDto requestDto, AuthCodeConfigModel oldConfig) {
    return AuthCodeConfigModel.builder()
        .ttl(coalesce(requestDto.getTtl(), oldConfig.getTtl()))
        .length(coalesce(requestDto.getLength(), oldConfig.getLength()))
        .build();
  }

  public Single<AuthCodeConfigModel> createAuthCodeConfig(
      String tenantId, CreateAuthCodeConfigRequestDto requestDto) {
    return createConfig(tenantId, requestDto);
  }

  public Single<AuthCodeConfigModel> getAuthCodeConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<AuthCodeConfigModel> updateAuthCodeConfig(
      String tenantId, UpdateAuthCodeConfigRequestDto requestDto) {
    return updateConfig(tenantId, requestDto);
  }

  public Completable deleteAuthCodeConfig(String tenantId) {
    return deleteConfig(tenantId);
  }
}
