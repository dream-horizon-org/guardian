package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GOOGLE_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.BaseConfigDao;
import com.dreamsportslabs.guardian.dao.config.GoogleConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.GoogleConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateGoogleConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateGoogleConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleConfigService
    extends BaseConfigService<
        GoogleConfigModel, CreateGoogleConfigRequestDto, UpdateGoogleConfigRequestDto> {
  private final GoogleConfigDao googleConfigDao;

  @Inject
  public GoogleConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      GoogleConfigDao googleConfigDao) {
    super(changelogService, mysqlClient, tenantCache);
    this.googleConfigDao = googleConfigDao;
  }

  @Override
  protected BaseConfigDao<GoogleConfigModel> getDao() {
    return googleConfigDao;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_GOOGLE_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return GOOGLE_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create Google config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update Google config";
  }

  @Override
  protected GoogleConfigModel mapToModel(CreateGoogleConfigRequestDto requestDto) {
    return GoogleConfigModel.builder()
        .clientId(requestDto.getClientId())
        .clientSecret(requestDto.getClientSecret())
        .build();
  }

  @Override
  protected GoogleConfigModel mergeModel(
      UpdateGoogleConfigRequestDto requestDto, GoogleConfigModel oldConfig) {
    return GoogleConfigModel.builder()
        .clientId(coalesce(requestDto.getClientId(), oldConfig.getClientId()))
        .clientSecret(coalesce(requestDto.getClientSecret(), oldConfig.getClientSecret()))
        .build();
  }

  public Single<GoogleConfigModel> createGoogleConfig(
      String tenantId, CreateGoogleConfigRequestDto requestDto) {
    return createConfig(tenantId, requestDto);
  }

  public Single<GoogleConfigModel> getGoogleConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<GoogleConfigModel> updateGoogleConfig(
      String tenantId, UpdateGoogleConfigRequestDto requestDto) {
    return updateConfig(tenantId, requestDto);
  }

  public Completable deleteGoogleConfig(String tenantId) {
    return deleteConfig(tenantId);
  }
}
