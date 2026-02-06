package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.CREATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.DELETE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.GET_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.UPDATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.EmailConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateEmailConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateEmailConfigRequestDto;
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
public class EmailConfigService
    extends BaseConfigService<
        EmailConfigModel, CreateEmailConfigRequestDto, UpdateEmailConfigRequestDto> {
  private final ObjectMapper objectMapper;

  @Inject
  public EmailConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      ObjectMapper objectMapper) {
    super(changelogService, mysqlClient, tenantCache);
    this.objectMapper = objectMapper;
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_EMAIL_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_EMAIL_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_EMAIL_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_EMAIL_CONFIG;
  }

  @Override
  protected Tuple buildParams(String tenantId, EmailConfigModel emailConfig) {
    return Tuple.tuple()
        .addValue(emailConfig.getIsSslEnabled())
        .addString(emailConfig.getHost())
        .addInteger(emailConfig.getPort())
        .addString(emailConfig.getSendEmailPath())
        .addString(emailConfig.getTemplateName())
        .addString(JsonUtils.serializeToJsonString(emailConfig.getTemplateParams(), objectMapper))
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return EMAIL_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_EMAIL_CONFIG;
  }

  @Override
  protected Class<EmailConfigModel> getModelClass() {
    return EmailConfigModel.class;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_EMAIL_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return EMAIL_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create email config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update email config";
  }

  @Override
  protected EmailConfigModel mapToModel(CreateEmailConfigRequestDto requestDto) {
    return EmailConfigModel.builder()
        .isSslEnabled(requestDto.getIsSslEnabled())
        .host(requestDto.getHost())
        .port(requestDto.getPort())
        .sendEmailPath(requestDto.getSendEmailPath())
        .templateName(requestDto.getTemplateName())
        .templateParams(requestDto.getTemplateParams())
        .build();
  }

  @Override
  protected EmailConfigModel mergeModel(
      UpdateEmailConfigRequestDto requestDto, EmailConfigModel oldConfig) {
    return EmailConfigModel.builder()
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), oldConfig.getIsSslEnabled()))
        .host(coalesce(requestDto.getHost(), oldConfig.getHost()))
        .port(coalesce(requestDto.getPort(), oldConfig.getPort()))
        .sendEmailPath(coalesce(requestDto.getSendEmailPath(), oldConfig.getSendEmailPath()))
        .templateName(coalesce(requestDto.getTemplateName(), oldConfig.getTemplateName()))
        .templateParams(coalesce(requestDto.getTemplateParams(), oldConfig.getTemplateParams()))
        .build();
  }

  public Single<EmailConfigModel> createEmailConfig(
      String tenantId, CreateEmailConfigRequestDto requestDto, String userIdentifier) {
    return createConfig(tenantId, requestDto, userIdentifier);
  }

  public Single<EmailConfigModel> getEmailConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<EmailConfigModel> updateEmailConfig(
      String tenantId, UpdateEmailConfigRequestDto requestDto, String userIdentifier) {
    return updateConfig(tenantId, requestDto, userIdentifier);
  }

  public Completable deleteEmailConfig(String tenantId, String userIdentifier) {
    return deleteConfig(tenantId, userIdentifier);
  }
}
