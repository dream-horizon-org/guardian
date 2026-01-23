package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.CREATE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.DELETE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.GET_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.UPDATE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GOOGLE_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.GoogleConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.google.inject.Inject;
import io.vertx.rxjava3.sqlclient.Tuple;

public class GoogleConfigDao extends BaseConfigDao<GoogleConfigModel> {

  @Inject
  public GoogleConfigDao(MysqlClient mysqlClient) {
    super(mysqlClient);
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_GOOGLE_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_GOOGLE_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_GOOGLE_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_GOOGLE_CONFIG;
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return GOOGLE_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_GOOGLE_CONFIG;
  }

  @Override
  protected Class<GoogleConfigModel> getModelClass() {
    return GoogleConfigModel.class;
  }

  @Override
  protected Tuple buildParams(String tenantId, GoogleConfigModel googleConfig) {
    return Tuple.tuple()
        .addString(googleConfig.getClientId())
        .addString(googleConfig.getClientSecret())
        .addString(tenantId);
  }
}
