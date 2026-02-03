package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.CREATE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.DELETE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.GET_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.UPDATE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.SmsConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateSmsConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateSmsConfigRequestDto;
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
public class SmsConfigService
    extends BaseConfigService<
        SmsConfigModel, CreateSmsConfigRequestDto, UpdateSmsConfigRequestDto> {
  private final ObjectMapper objectMapper;

  @Inject
  public SmsConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      ObjectMapper objectMapper) {
    super(changelogService, mysqlClient, tenantCache);
    this.objectMapper = objectMapper;
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_SMS_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_SMS_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_SMS_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_SMS_CONFIG;
  }

  @Override
  protected Tuple buildParams(String tenantId, SmsConfigModel smsConfig) {
    return Tuple.tuple()
        .addValue(smsConfig.getIsSslEnabled())
        .addString(smsConfig.getHost())
        .addInteger(smsConfig.getPort())
        .addString(smsConfig.getSendSmsPath())
        .addString(smsConfig.getTemplateName())
        .addString(JsonUtils.serializeToJsonString(smsConfig.getTemplateParams(), objectMapper))
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return SMS_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_SMS_CONFIG;
  }

  @Override
  protected Class<SmsConfigModel> getModelClass() {
    return SmsConfigModel.class;
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
