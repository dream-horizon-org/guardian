package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_FB_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FB_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.BaseConfigDao;
import com.dreamsportslabs.guardian.dao.config.FbConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.FbConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateFbConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateFbConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FbConfigService
    extends BaseConfigService<FbConfigModel, CreateFbConfigRequestDto, UpdateFbConfigRequestDto> {
  private final FbConfigDao fbConfigDao;

  @Inject
  public FbConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      FbConfigDao fbConfigDao) {
    super(changelogService, mysqlClient, tenantCache);
    this.fbConfigDao = fbConfigDao;
  }

  @Override
  protected BaseConfigDao<FbConfigModel> getDao() {
    return fbConfigDao;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_FB_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return FB_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create FB config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update FB config";
  }

  @Override
  protected FbConfigModel mapToModel(CreateFbConfigRequestDto requestDto) {
    return FbConfigModel.builder()
        .appId(requestDto.getAppId())
        .appSecret(requestDto.getAppSecret())
        .sendAppSecret(requestDto.getSendAppSecret())
        .build();
  }

  @Override
  protected FbConfigModel mergeModel(UpdateFbConfigRequestDto requestDto, FbConfigModel oldConfig) {
    return FbConfigModel.builder()
        .appId(coalesce(requestDto.getAppId(), oldConfig.getAppId()))
        .appSecret(coalesce(requestDto.getAppSecret(), oldConfig.getAppSecret()))
        .sendAppSecret(coalesce(requestDto.getSendAppSecret(), oldConfig.getSendAppSecret()))
        .build();
  }

  public Single<FbConfigModel> createFbConfig(
      String tenantId, CreateFbConfigRequestDto requestDto) {
    return createConfig(tenantId, requestDto);
  }

  public Single<FbConfigModel> getFbConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<FbConfigModel> updateFbConfig(
      String tenantId, UpdateFbConfigRequestDto requestDto) {
    return updateConfig(tenantId, requestDto);
  }

  public Completable deleteFbConfig(String tenantId) {
    return deleteConfig(tenantId);
  }
}
