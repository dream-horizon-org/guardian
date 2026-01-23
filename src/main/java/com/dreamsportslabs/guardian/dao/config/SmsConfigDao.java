package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.CREATE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.DELETE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.GET_SMS_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.SmsConfigQuery.UPDATE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.SmsConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.vertx.rxjava3.sqlclient.Tuple;

public class SmsConfigDao extends BaseConfigDao<SmsConfigModel> {
  private final ObjectMapper objectMapper;

  @Inject
  public SmsConfigDao(MysqlClient mysqlClient, ObjectMapper objectMapper) {
    super(mysqlClient);
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
}
