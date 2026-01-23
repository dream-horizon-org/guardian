package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_TENANT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_TENANT_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_NAME;
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
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.dreamsportslabs.guardian.utils.SqlUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TenantDao {
  private final MysqlClient mysqlClient;

  public Single<TenantModel> createTenant(SqlConnection client, TenantModel tenant) {
    Tuple params = Tuple.tuple().addString(tenant.getId()).addString(tenant.getName());

    return client
        .preparedQuery(CREATE_TENANT)
        .rxExecute(params)
        .map(result -> tenant)
        .onErrorResumeNext(err -> handleTenantError(err, tenant.getName(), tenant.getId()));
  }

  public Maybe<TenantModel> getTenant(String tenantId) {
    return executeTenantQuery(GET_TENANT, Tuple.of(tenantId));
  }

  public Maybe<TenantModel> getTenantByName(String name) {
    return executeTenantQuery(GET_TENANT_BY_NAME, Tuple.of(name));
  }

  private Maybe<TenantModel> executeTenantQuery(String query, Tuple params) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(query)
        .rxExecute(params)
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, TenantModel.class).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateTenant(SqlConnection client, String tenantId, String name) {
    return client
        .preparedQuery(UPDATE_TENANT)
        .rxExecute(Tuple.of(name, tenantId))
        .ignoreElement()
        .onErrorResumeNext(
            err ->
                SqlUtils.handleMySqlError(
                        err,
                        TENANT_NAME_ALREADY_EXISTS,
                        String.format("%s: %s", DUPLICATE_ENTRY_MESSAGE_TENANT_NAME, name),
                        INTERNAL_SERVER_ERROR)
                    .ignoreElement());
  }

  public Single<Boolean> deleteTenant(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(DELETE_TENANT)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private <T> Single<T> handleTenantError(Throwable err, String name, String tenantId) {
    if (err instanceof MySQLException mySQLException
        && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
      String errorMessage = mySQLException.getMessage();
      if (errorMessage != null && errorMessage.contains(TENANT_NAME)) {
        return Single.error(
            TENANT_NAME_ALREADY_EXISTS.getCustomException(
                String.format("%s: %s", DUPLICATE_ENTRY_MESSAGE_TENANT_NAME, name)));
      }
      return Single.error(
          TENANT_ALREADY_EXISTS.getCustomException(
              String.format("%s: %s", DUPLICATE_ENTRY_MESSAGE_TENANT_ID, tenantId)));
    }
    return Single.error(INTERNAL_SERVER_ERROR.getException(err));
  }
}
