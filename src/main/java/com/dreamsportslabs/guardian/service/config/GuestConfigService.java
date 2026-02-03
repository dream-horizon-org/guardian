package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.CREATE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.DELETE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.GET_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.UPDATE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GUEST_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GUEST_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.GuestConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateGuestConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateGuestConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GuestConfigService
    extends BaseConfigService<
        GuestConfigModel, CreateGuestConfigRequestDto, UpdateGuestConfigRequestDto> {
  private final ObjectMapper objectMapper;

  @Inject
  public GuestConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      ObjectMapper objectMapper) {
    super(changelogService, mysqlClient, tenantCache);
    this.objectMapper = objectMapper;
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_GUEST_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_GUEST_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_GUEST_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_GUEST_CONFIG;
  }

  @Override
  protected Tuple buildParams(String tenantId, GuestConfigModel guestConfig) {
    return Tuple.tuple()
        .addValue(guestConfig.getIsEncrypted())
        .addString(guestConfig.getSecretKey())
        .addString(JsonUtils.serializeToJsonString(guestConfig.getAllowedScopes(), objectMapper))
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return GUEST_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_GUEST_CONFIG;
  }

  @Override
  protected Class<GuestConfigModel> getModelClass() {
    return GuestConfigModel.class;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_GUEST_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return GUEST_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create guest config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update guest config";
  }

  @Override
  protected GuestConfigModel mapToModel(CreateGuestConfigRequestDto requestDto) {
    return GuestConfigModel.builder()
        .isEncrypted(requestDto.getIsEncrypted())
        .secretKey(requestDto.getSecretKey())
        .allowedScopes(requestDto.getAllowedScopes())
        .build();
  }

  @Override
  protected GuestConfigModel mergeModel(
      UpdateGuestConfigRequestDto requestDto, GuestConfigModel oldConfig) {
    return GuestConfigModel.builder()
        .isEncrypted(coalesce(requestDto.getIsEncrypted(), oldConfig.getIsEncrypted()))
        .secretKey(coalesce(requestDto.getSecretKey(), oldConfig.getSecretKey()))
        .allowedScopes(coalesce(requestDto.getAllowedScopes(), oldConfig.getAllowedScopes()))
        .build();
  }

  public Single<GuestConfigModel> createGuestConfig(
      String tenantId, CreateGuestConfigRequestDto requestDto) {
    return createConfig(tenantId, requestDto);
  }

  public Single<GuestConfigModel> getGuestConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<GuestConfigModel> updateGuestConfig(
      String tenantId, UpdateGuestConfigRequestDto requestDto) {
    return updateConfig(tenantId, requestDto);
  }

  public Completable deleteGuestConfig(String tenantId) {
    return deleteConfig(tenantId);
  }
}
