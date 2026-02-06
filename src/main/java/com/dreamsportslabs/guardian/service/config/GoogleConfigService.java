package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.CREATE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.DELETE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.GET_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.UPDATE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GOOGLE_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GOOGLE_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.GoogleConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateGoogleConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateGoogleConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleConfigService
    extends BaseConfigService<
        GoogleConfigModel, CreateGoogleConfigRequestDto, UpdateGoogleConfigRequestDto> {

  @Inject
  public GoogleConfigService(
      ChangelogService changelogService, MysqlClient mysqlClient, TenantCache tenantCache) {
    super(changelogService, mysqlClient, tenantCache);
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_GOOGLE_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_GOOGLE_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_GOOGLE_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_GOOGLE_CONFIG;
  }

  @Override
  protected Tuple buildParams(String tenantId, GoogleConfigModel googleConfig) {
    return Tuple.tuple()
        .addString(googleConfig.getClientId())
        .addString(googleConfig.getClientSecret())
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return GOOGLE_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_GOOGLE_CONFIG;
  }

  @Override
  protected Class<GoogleConfigModel> getModelClass() {
    return GoogleConfigModel.class;
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
      String tenantId, CreateGoogleConfigRequestDto requestDto, String userIdentifier) {
    return createConfig(tenantId, requestDto, userIdentifier);
  }

  public Single<GoogleConfigModel> getGoogleConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<GoogleConfigModel> updateGoogleConfig(
      String tenantId, UpdateGoogleConfigRequestDto requestDto, String userIdentifier) {
    return updateConfig(tenantId, requestDto, userIdentifier);
  }

  public Completable deleteGoogleConfig(String tenantId, String userIdentifier) {
    return deleteConfig(tenantId, userIdentifier);
  }
}
