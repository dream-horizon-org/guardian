package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_USER_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_ADD_PROVIDER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_AUTHENTICATE_USER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_CREATE_USER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_GET_USER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_HOST;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_PORT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_SEND_PROVIDER_DETAILS;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_UPDATE_USER_PATH;
import static com.dreamsportslabs.guardian.dao.config.query.UserConfigQuery.CREATE_USER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.UserConfigQuery.GET_USER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.UserConfigQuery.UPDATE_USER_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.UserConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.UpdateUserConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserConfigService
    extends BaseConfigService<UserConfigModel, UserConfigModel, UpdateUserConfigRequestDto> {

  @Inject
  public UserConfigService(
      ChangelogService changelogService, MysqlClient mysqlClient, TenantCache tenantCache) {
    super(changelogService, mysqlClient, tenantCache);
  }

  public Completable createDefaultUserConfig(SqlConnection client, String tenantId) {
    UserConfigModel userConfigModel = buildDefaultUserConfig(tenantId);
    return getDao().createConfig(client, tenantId, userConfigModel).ignoreElement();
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_USER_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_USER_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_USER_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return "";
  }

  @Override
  protected Tuple buildParams(String tenantId, UserConfigModel userConfig) {
    return Tuple.tuple()
        .addValue(userConfig.getIsSslEnabled())
        .addString(userConfig.getHost())
        .addInteger(userConfig.getPort())
        .addString(userConfig.getGetUserPath())
        .addString(userConfig.getCreateUserPath())
        .addString(userConfig.getAuthenticateUserPath())
        .addString(userConfig.getAddProviderPath())
        .addString(userConfig.getUpdateUserPath())
        .addValue(userConfig.getSendProviderDetails())
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return INTERNAL_SERVER_ERROR;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return "User config already exists";
  }

  @Override
  protected Class<UserConfigModel> getModelClass() {
    return UserConfigModel.class;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_USER_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return USER_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create user config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update user config";
  }

  @Override
  protected UserConfigModel mapToModel(UserConfigModel requestDto) {
    return requestDto;
  }

  @Override
  protected UserConfigModel mergeModel(
      UpdateUserConfigRequestDto requestDto, UserConfigModel oldConfig) {
    return UserConfigModel.builder()
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), oldConfig.getIsSslEnabled()))
        .host(coalesce(requestDto.getHost(), oldConfig.getHost()))
        .port(coalesce(requestDto.getPort(), oldConfig.getPort()))
        .getUserPath(coalesce(requestDto.getGetUserPath(), oldConfig.getGetUserPath()))
        .createUserPath(coalesce(requestDto.getCreateUserPath(), oldConfig.getCreateUserPath()))
        .authenticateUserPath(
            coalesce(requestDto.getAuthenticateUserPath(), oldConfig.getAuthenticateUserPath()))
        .addProviderPath(coalesce(requestDto.getAddProviderPath(), oldConfig.getAddProviderPath()))
        .updateUserPath(coalesce(requestDto.getUpdateUserPath(), oldConfig.getUpdateUserPath()))
        .sendProviderDetails(
            coalesce(requestDto.getSendProviderDetails(), oldConfig.getSendProviderDetails()))
        .build();
  }

  public Single<UserConfigModel> getUserConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<UserConfigModel> updateUserConfig(
      String tenantId, UpdateUserConfigRequestDto requestDto) {
    return updateConfig(tenantId, requestDto);
  }

  UserConfigModel buildDefaultUserConfig(String tenantId) {
    return UserConfigModel.builder()
        .host(DEFAULT_USER_CONFIG_HOST)
        .port(DEFAULT_USER_CONFIG_PORT)
        .isSslEnabled(DEFAULT_USER_CONFIG_IS_SSL_ENABLED)
        .getUserPath(DEFAULT_USER_CONFIG_GET_USER_PATH)
        .createUserPath(DEFAULT_USER_CONFIG_CREATE_USER_PATH)
        .authenticateUserPath(DEFAULT_USER_CONFIG_AUTHENTICATE_USER_PATH)
        .addProviderPath(DEFAULT_USER_CONFIG_ADD_PROVIDER_PATH)
        .updateUserPath(DEFAULT_USER_CONFIG_UPDATE_USER_PATH)
        .sendProviderDetails(DEFAULT_USER_CONFIG_SEND_PROVIDER_DETAILS)
        .build();
  }
}
