package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.CREATE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.DELETE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.GET_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.UPDATE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OTP_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.OtpConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.vertx.rxjava3.sqlclient.Tuple;

public class OtpConfigDao extends BaseConfigDao<OtpConfigModel> {
  private final ObjectMapper objectMapper;

  @Inject
  public OtpConfigDao(MysqlClient mysqlClient, ObjectMapper objectMapper) {
    super(mysqlClient);
    this.objectMapper = objectMapper;
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_OTP_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_OTP_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_OTP_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_OTP_CONFIG;
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return OTP_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_OTP_CONFIG;
  }

  @Override
  protected Class<OtpConfigModel> getModelClass() {
    return OtpConfigModel.class;
  }

  @Override
  protected Tuple buildParams(String tenantId, OtpConfigModel otpConfig) {
    return Tuple.tuple()
        .addValue(otpConfig.getIsOtpMocked())
        .addInteger(otpConfig.getOtpLength())
        .addInteger(otpConfig.getTryLimit())
        .addInteger(otpConfig.getResendLimit())
        .addInteger(otpConfig.getOtpResendInterval())
        .addInteger(otpConfig.getOtpValidity())
        .addInteger(otpConfig.getOtpSendWindowSeconds())
        .addInteger(otpConfig.getOtpSendWindowMaxCount())
        .addInteger(otpConfig.getOtpSendBlockSeconds())
        .addString(JsonUtils.serializeToJsonString(otpConfig.getWhitelistedInputs(), objectMapper))
        .addString(tenantId);
  }
}
