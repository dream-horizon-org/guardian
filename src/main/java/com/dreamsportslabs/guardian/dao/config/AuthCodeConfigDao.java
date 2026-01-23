package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.CREATE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.DELETE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.GET_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.UPDATE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.AUTH_CODE_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.AuthCodeConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.google.inject.Inject;
import io.vertx.rxjava3.sqlclient.Tuple;

public class AuthCodeConfigDao extends BaseConfigDao<AuthCodeConfigModel> {

  @Inject
  public AuthCodeConfigDao(MysqlClient mysqlClient) {
    super(mysqlClient);
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_AUTH_CODE_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_AUTH_CODE_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_AUTH_CODE_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_AUTH_CODE_CONFIG;
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return AUTH_CODE_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_AUTH_CODE_CONFIG;
  }

  @Override
  protected Class<AuthCodeConfigModel> getModelClass() {
    return AuthCodeConfigModel.class;
  }

  @Override
  protected Tuple buildParams(String tenantId, AuthCodeConfigModel authCodeConfig) {
    return Tuple.tuple()
        .addInteger(authCodeConfig.getTtl())
        .addInteger(authCodeConfig.getLength())
        .addString(tenantId);
  }
}
