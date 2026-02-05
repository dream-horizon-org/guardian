package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_FB_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.CREATE_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.DELETE_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.GET_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.UPDATE_FB_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FB_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FB_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.FbConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateFbConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateFbConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FbConfigService
    extends BaseConfigService<FbConfigModel, CreateFbConfigRequestDto, UpdateFbConfigRequestDto> {

  @Inject
  public FbConfigService(
      ChangelogService changelogService, MysqlClient mysqlClient, TenantCache tenantCache) {
    super(changelogService, mysqlClient, tenantCache);
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_FB_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_FB_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_FB_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_FB_CONFIG;
  }

  @Override
  protected Tuple buildParams(String tenantId, FbConfigModel fbConfig) {
    return Tuple.tuple()
        .addString(fbConfig.getAppId())
        .addString(fbConfig.getAppSecret())
        .addValue(fbConfig.getSendAppSecret())
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return FB_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_FB_CONFIG;
  }

  @Override
  protected Class<FbConfigModel> getModelClass() {
    return FbConfigModel.class;
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
      String tenantId, CreateFbConfigRequestDto requestDto, String userIdentifier) {
    return createConfig(tenantId, requestDto, userIdentifier);
  }

  public Single<FbConfigModel> getFbConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<FbConfigModel> updateFbConfig(
      String tenantId, UpdateFbConfigRequestDto requestDto, String userIdentifier) {
    return updateConfig(tenantId, requestDto, userIdentifier);
  }

  public Completable deleteFbConfig(String tenantId, String userIdentifier) {
    return deleteConfig(tenantId, userIdentifier);
  }
}
