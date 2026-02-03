package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.CREATE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.DELETE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.GET_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.UPDATE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.ADMIN_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.ADMIN_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.AdminConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateAdminConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateAdminConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdminConfigService
    extends BaseConfigService<
        AdminConfigModel, CreateAdminConfigRequestDto, UpdateAdminConfigRequestDto> {

  @Inject
  public AdminConfigService(
      ChangelogService changelogService, MysqlClient mysqlClient, TenantCache tenantCache) {
    super(changelogService, mysqlClient, tenantCache);
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_ADMIN_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_ADMIN_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_ADMIN_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_ADMIN_CONFIG;
  }

  @Override
  protected Tuple buildParams(String tenantId, AdminConfigModel adminConfig) {
    return Tuple.tuple()
        .addString(adminConfig.getUsername())
        .addString(adminConfig.getPassword())
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return ADMIN_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_ADMIN_CONFIG;
  }

  @Override
  protected Class<AdminConfigModel> getModelClass() {
    return AdminConfigModel.class;
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
