package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.ADMIN_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.AdminConfigDao;
import com.dreamsportslabs.guardian.dao.config.BaseConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.AdminConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateAdminConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateAdminConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdminConfigService
    extends BaseConfigService<
        AdminConfigModel, CreateAdminConfigRequestDto, UpdateAdminConfigRequestDto> {
  private final AdminConfigDao adminConfigDao;

  @Inject
  public AdminConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      AdminConfigDao adminConfigDao) {
    super(changelogService, mysqlClient, tenantCache);
    this.adminConfigDao = adminConfigDao;
  }

  @Override
  protected BaseConfigDao<AdminConfigModel> getDao() {
    return adminConfigDao;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_ADMIN_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return ADMIN_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create admin config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update admin config";
  }

  @Override
  protected AdminConfigModel mapToModel(CreateAdminConfigRequestDto requestDto) {
    return AdminConfigModel.builder()
        .username(requestDto.getUsername())
        .password(requestDto.getPassword())
        .build();
  }

  @Override
  protected AdminConfigModel mergeModel(
      UpdateAdminConfigRequestDto requestDto, AdminConfigModel oldConfig) {
    return AdminConfigModel.builder()
        .username(coalesce(requestDto.getUsername(), oldConfig.getUsername()))
        .password(coalesce(requestDto.getPassword(), oldConfig.getPassword()))
        .build();
  }

  public Single<AdminConfigModel> createAdminConfig(
      String tenantId, CreateAdminConfigRequestDto requestDto) {
    return createConfig(tenantId, requestDto);
  }

  public Single<AdminConfigModel> getAdminConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<AdminConfigModel> updateAdminConfig(
      String tenantId, UpdateAdminConfigRequestDto requestDto) {
    return updateConfig(tenantId, requestDto);
  }

  public Completable deleteAdminConfig(String tenantId) {
    return deleteConfig(tenantId);
  }
}
