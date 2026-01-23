package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.CREATE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.DELETE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.GET_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.UPDATE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.ADMIN_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.AdminConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.google.inject.Inject;
import io.vertx.rxjava3.sqlclient.Tuple;

public class AdminConfigDao extends BaseConfigDao<AdminConfigModel> {

  @Inject
  public AdminConfigDao(MysqlClient mysqlClient) {
    super(mysqlClient);
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_ADMIN_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_ADMIN_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_ADMIN_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_ADMIN_CONFIG;
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return ADMIN_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_ADMIN_CONFIG;
  }

  @Override
  protected Class<AdminConfigModel> getModelClass() {
    return AdminConfigModel.class;
  }

  @Override
  protected Tuple buildParams(String tenantId, AdminConfigModel adminConfig) {
    return Tuple.tuple()
        .addString(adminConfig.getUsername())
        .addString(adminConfig.getPassword())
        .addString(tenantId);
  }
}
