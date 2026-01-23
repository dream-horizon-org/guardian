package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.CREATE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.DELETE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.GET_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.UPDATE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GUEST_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.GuestConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.vertx.rxjava3.sqlclient.Tuple;

public class GuestConfigDao extends BaseConfigDao<GuestConfigModel> {
  private final ObjectMapper objectMapper;

  @Inject
  public GuestConfigDao(MysqlClient mysqlClient, ObjectMapper objectMapper) {
    super(mysqlClient);
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
  protected Tuple buildParams(String tenantId, GuestConfigModel guestConfig) {
    return Tuple.tuple()
        .addValue(guestConfig.getIsEncrypted())
        .addString(guestConfig.getSecretKey())
        .addString(JsonUtils.serializeToJsonString(guestConfig.getAllowedScopes(), objectMapper))
        .addString(tenantId);
  }
}
