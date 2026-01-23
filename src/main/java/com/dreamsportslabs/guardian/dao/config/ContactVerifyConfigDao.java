package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.CREATE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.DELETE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.GET_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.UPDATE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CONTACT_VERIFY_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.ContactVerifyConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.vertx.rxjava3.sqlclient.Tuple;

public class ContactVerifyConfigDao extends BaseConfigDao<ContactVerifyConfigModel> {
  private final ObjectMapper objectMapper;

  @Inject
  public ContactVerifyConfigDao(MysqlClient mysqlClient, ObjectMapper objectMapper) {
    super(mysqlClient);
    this.objectMapper = objectMapper;
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_CONTACT_VERIFY_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_CONTACT_VERIFY_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_CONTACT_VERIFY_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_CONTACT_VERIFY_CONFIG;
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return CONTACT_VERIFY_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_CONTACT_VERIFY_CONFIG;
  }

  @Override
  protected Class<ContactVerifyConfigModel> getModelClass() {
    return ContactVerifyConfigModel.class;
  }

  @Override
  protected Tuple buildParams(String tenantId, ContactVerifyConfigModel contactVerifyConfig) {
    return Tuple.tuple()
        .addValue(contactVerifyConfig.getIsOtpMocked())
        .addValue(contactVerifyConfig.getOtpLength())
        .addValue(contactVerifyConfig.getTryLimit())
        .addValue(contactVerifyConfig.getResendLimit())
        .addValue(contactVerifyConfig.getOtpResendInterval())
        .addValue(contactVerifyConfig.getOtpValidity())
        .addString(
            JsonUtils.serializeToJsonString(
                contactVerifyConfig.getWhitelistedInputs(), objectMapper))
        .addString(tenantId);
  }
}
