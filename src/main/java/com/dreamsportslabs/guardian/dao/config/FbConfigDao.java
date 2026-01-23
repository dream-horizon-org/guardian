package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.CREATE_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.DELETE_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.GET_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.UPDATE_FB_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FB_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.FbConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.google.inject.Inject;
import io.vertx.rxjava3.sqlclient.Tuple;

public class FbConfigDao extends BaseConfigDao<FbConfigModel> {

  @Inject
  public FbConfigDao(MysqlClient mysqlClient) {
    super(mysqlClient);
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_FB_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_FB_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_FB_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_FB_CONFIG;
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return FB_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_FB_CONFIG;
  }

  @Override
  protected Class<FbConfigModel> getModelClass() {
    return FbConfigModel.class;
  }

  @Override
  protected Tuple buildParams(String tenantId, FbConfigModel fbConfig) {
    return Tuple.tuple()
        .addString(fbConfig.getAppId())
        .addString(fbConfig.getAppSecret())
        .addValue(fbConfig.getSendAppSecret())
        .addString(tenantId);
  }
}
