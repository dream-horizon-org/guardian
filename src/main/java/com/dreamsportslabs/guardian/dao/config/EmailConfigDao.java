package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.CREATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.DELETE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.GET_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.EmailConfigQuery.UPDATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.EmailConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.vertx.rxjava3.sqlclient.Tuple;

public class EmailConfigDao extends BaseConfigDao<EmailConfigModel> {
  private final ObjectMapper objectMapper;

  @Inject
  public EmailConfigDao(MysqlClient mysqlClient, ObjectMapper objectMapper) {
    super(mysqlClient);
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
}
