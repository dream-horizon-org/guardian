package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_TENANT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_TENANT_NAME;
import static com.dreamsportslabs.guardian.dao.config.query.TenantQuery.CREATE_TENANT;
import static com.dreamsportslabs.guardian.dao.config.query.TenantQuery.DELETE_TENANT;
import static com.dreamsportslabs.guardian.dao.config.query.TenantQuery.GET_TENANT;
import static com.dreamsportslabs.guardian.dao.config.query.TenantQuery.GET_TENANT_BY_NAME;
import static com.dreamsportslabs.guardian.dao.config.query.TenantQuery.UPDATE_TENANT;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_NAME_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.TenantModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.dreamsportslabs.guardian.utils.SqlUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantDao extends BaseConfigDao<TenantModel> {
  @Inject
  public TenantDao(MysqlClient mysqlClient) {
    super(mysqlClient);
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_TENANT;
  }

  @Override
  protected String getGetQuery() {
    return GET_TENANT;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_TENANT;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_TENANT;
  }

  @Override
  protected Tuple buildParams(String tenantId, TenantModel model) {
    return Tuple.tuple().addString(model.getId()).addString(model.getName());
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return TENANT_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_TENANT_ID;
  }

  @Override
  protected Class<TenantModel> getModelClass() {
    return TenantModel.class;
  }

  @Override
  public Single<TenantModel> createConfig(
      SqlConnection client, String tenantId, TenantModel model) {
    return client
        .preparedQuery(getCreateQuery())
        .rxExecute(buildParams(tenantId, model))
        .map(result -> model)
        .onErrorResumeNext(err -> SqlUtils.handleTenantError(err, model.getName(), model.getId()));
  }

  @Override
  public Completable updateConfig(SqlConnection client, String tenantId, TenantModel model) {
    return client
        .preparedQuery(getUpdateQuery())
        .rxExecute(Tuple.of(model.getName(), tenantId))
        .ignoreElement()
        .onErrorResumeNext(
            err ->
                SqlUtils.handleMySqlError(
                        err,
                        TENANT_NAME_ALREADY_EXISTS,
                        String.format(
                            "%s: %s", DUPLICATE_ENTRY_MESSAGE_TENANT_NAME, model.getName()),
                        INTERNAL_SERVER_ERROR)
                    .ignoreElement());
  }

  public Maybe<TenantModel> getTenantByName(String name) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_TENANT_BY_NAME)
        .rxExecute(Tuple.of(name))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, getModelClass()).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}
