package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.CREATE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.DELETE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.GET_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.UPDATE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.AUTH_CODE_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.AUTH_CODE_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.AuthCodeConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateAuthCodeConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateAuthCodeConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthCodeConfigService
    extends BaseConfigService<
        AuthCodeConfigModel, CreateAuthCodeConfigRequestDto, UpdateAuthCodeConfigRequestDto> {

  @Inject
  public AuthCodeConfigService(
      ChangelogService changelogService, MysqlClient mysqlClient, TenantCache tenantCache) {
    super(changelogService, mysqlClient, tenantCache);
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_AUTH_CODE_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_AUTH_CODE_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_AUTH_CODE_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_AUTH_CODE_CONFIG;
  }

  @Override
  protected Tuple buildParams(String tenantId, AuthCodeConfigModel authCodeConfig) {
    return Tuple.tuple()
        .addInteger(authCodeConfig.getTtl())
        .addInteger(authCodeConfig.getLength())
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return AUTH_CODE_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_AUTH_CODE_CONFIG;
  }

  @Override
  protected Class<AuthCodeConfigModel> getModelClass() {
    return AuthCodeConfigModel.class;
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
      String tenantId, CreateAuthCodeConfigRequestDto requestDto, String userIdentifier) {
    return createConfig(tenantId, requestDto, userIdentifier);
  }

  public Single<AuthCodeConfigModel> getAuthCodeConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<AuthCodeConfigModel> updateAuthCodeConfig(
      String tenantId, UpdateAuthCodeConfigRequestDto requestDto, String userIdentifier) {
    return updateConfig(tenantId, requestDto, userIdentifier);
  }

  public Completable deleteAuthCodeConfig(String tenantId, String userIdentifier) {
    return deleteConfig(tenantId, userIdentifier);
  }
}
