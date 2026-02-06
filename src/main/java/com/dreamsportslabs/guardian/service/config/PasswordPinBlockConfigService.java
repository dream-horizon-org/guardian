package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_PASSWORD_PIN_BLOCK_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_PASSWORD_PIN_BLOCK_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.PasswordPinBlockConfigQuery.CREATE_PASSWORD_PIN_BLOCK_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.PasswordPinBlockConfigQuery.DELETE_PASSWORD_PIN_BLOCK_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.PasswordPinBlockConfigQuery.GET_PASSWORD_PIN_BLOCK_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.PasswordPinBlockConfigQuery.UPDATE_PASSWORD_PIN_BLOCK_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.PASSWORD_PIN_BLOCK_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.PASSWORD_PIN_BLOCK_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.PasswordPinBlockConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreatePasswordPinBlockConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdatePasswordPinBlockConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PasswordPinBlockConfigService
    extends BaseConfigService<
        PasswordPinBlockConfigModel,
        CreatePasswordPinBlockConfigRequestDto,
        UpdatePasswordPinBlockConfigRequestDto> {

  @Inject
  public PasswordPinBlockConfigService(
      ChangelogService changelogService, MysqlClient mysqlClient, TenantCache tenantCache) {
    super(changelogService, mysqlClient, tenantCache);
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_PASSWORD_PIN_BLOCK_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_PASSWORD_PIN_BLOCK_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_PASSWORD_PIN_BLOCK_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_PASSWORD_PIN_BLOCK_CONFIG;
  }

  @Override
  protected Tuple buildParams(String tenantId, PasswordPinBlockConfigModel model) {
    return Tuple.tuple()
        .addInteger(model.getAttemptsAllowed())
        .addInteger(model.getAttemptsWindowSeconds())
        .addInteger(model.getBlockIntervalSeconds())
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return PASSWORD_PIN_BLOCK_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_PASSWORD_PIN_BLOCK_CONFIG;
  }

  @Override
  protected Class<PasswordPinBlockConfigModel> getModelClass() {
    return PasswordPinBlockConfigModel.class;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_PASSWORD_PIN_BLOCK_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return PASSWORD_PIN_BLOCK_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create password pin block config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update password pin block config";
  }

  @Override
  protected PasswordPinBlockConfigModel mapToModel(
      CreatePasswordPinBlockConfigRequestDto requestDto) {
    return PasswordPinBlockConfigModel.builder()
        .attemptsAllowed(requestDto.getAttemptsAllowed())
        .attemptsWindowSeconds(requestDto.getAttemptsWindowSeconds())
        .blockIntervalSeconds(requestDto.getBlockIntervalSeconds())
        .build();
  }

  @Override
  protected PasswordPinBlockConfigModel mergeModel(
      UpdatePasswordPinBlockConfigRequestDto requestDto, PasswordPinBlockConfigModel oldConfig) {
    return PasswordPinBlockConfigModel.builder()
        .attemptsAllowed(coalesce(requestDto.getAttemptsAllowed(), oldConfig.getAttemptsAllowed()))
        .attemptsWindowSeconds(
            coalesce(requestDto.getAttemptsWindowSeconds(), oldConfig.getAttemptsWindowSeconds()))
        .blockIntervalSeconds(
            coalesce(requestDto.getBlockIntervalSeconds(), oldConfig.getBlockIntervalSeconds()))
        .build();
  }

  public Single<PasswordPinBlockConfigModel> getPasswordPinBlockConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<PasswordPinBlockConfigModel> updatePasswordPinBlockConfig(
      String tenantId, UpdatePasswordPinBlockConfigRequestDto requestDto, String userIdentifier) {
    return updateConfig(tenantId, requestDto, userIdentifier);
  }

  public Completable deletePasswordPinBlockConfig(String tenantId, String userIdentifier) {
    return deleteConfig(tenantId, userIdentifier);
  }
}
