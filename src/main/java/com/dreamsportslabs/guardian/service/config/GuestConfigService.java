package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GUEST_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.BaseConfigDao;
import com.dreamsportslabs.guardian.dao.config.GuestConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.GuestConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateGuestConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateGuestConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GuestConfigService
    extends BaseConfigService<
        GuestConfigModel, CreateGuestConfigRequestDto, UpdateGuestConfigRequestDto> {
  private final GuestConfigDao guestConfigDao;

  @Inject
  public GuestConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      GuestConfigDao guestConfigDao) {
    super(changelogService, mysqlClient, tenantCache);
    this.guestConfigDao = guestConfigDao;
  }

  @Override
  protected BaseConfigDao<GuestConfigModel> getDao() {
    return guestConfigDao;
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
