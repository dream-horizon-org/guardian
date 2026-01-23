package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.BaseConfigDao;
import com.dreamsportslabs.guardian.dao.config.SmsConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.SmsConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateSmsConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateSmsConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmsConfigService
    extends BaseConfigService<
        SmsConfigModel, CreateSmsConfigRequestDto, UpdateSmsConfigRequestDto> {
  private final SmsConfigDao smsConfigDao;

  @Inject
  public SmsConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      SmsConfigDao smsConfigDao) {
    super(changelogService, mysqlClient, tenantCache);
    this.smsConfigDao = smsConfigDao;
  }

  @Override
  protected BaseConfigDao<SmsConfigModel> getDao() {
    return smsConfigDao;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_SMS_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return SMS_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create SMS config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update SMS config";
  }

  @Override
  protected SmsConfigModel mapToModel(CreateSmsConfigRequestDto requestDto) {
    return SmsConfigModel.builder()
        .isSslEnabled(requestDto.getIsSslEnabled())
        .host(requestDto.getHost())
        .port(requestDto.getPort())
        .sendSmsPath(requestDto.getSendSmsPath())
        .templateName(requestDto.getTemplateName())
        .templateParams(requestDto.getTemplateParams())
        .build();
  }

  @Override
  protected SmsConfigModel mergeModel(
      UpdateSmsConfigRequestDto requestDto, SmsConfigModel oldConfig) {
    return SmsConfigModel.builder()
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), oldConfig.getIsSslEnabled()))
        .host(coalesce(requestDto.getHost(), oldConfig.getHost()))
        .port(coalesce(requestDto.getPort(), oldConfig.getPort()))
        .sendSmsPath(coalesce(requestDto.getSendSmsPath(), oldConfig.getSendSmsPath()))
        .templateName(coalesce(requestDto.getTemplateName(), oldConfig.getTemplateName()))
        .templateParams(coalesce(requestDto.getTemplateParams(), oldConfig.getTemplateParams()))
        .build();
  }

  public Single<SmsConfigModel> createSmsConfig(
      String tenantId, CreateSmsConfigRequestDto requestDto) {
    return createConfig(tenantId, requestDto);
  }

  public Single<SmsConfigModel> getSmsConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<SmsConfigModel> updateSmsConfig(
      String tenantId, UpdateSmsConfigRequestDto requestDto) {
    return updateConfig(tenantId, requestDto);
  }

  public Completable deleteSmsConfig(String tenantId) {
    return deleteConfig(tenantId);
  }
}
