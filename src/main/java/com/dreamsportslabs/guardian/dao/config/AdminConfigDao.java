package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.CREATE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.DELETE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.GET_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AdminConfigQuery.UPDATE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.*;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.AdminConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.dreamsportslabs.guardian.utils.SqlUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AdminConfigDao {
  private final MysqlClient mysqlClient;

  public Single<AdminConfigModel> createAdminConfig(
      SqlConnection client, String tenantId, AdminConfigModel adminConfig) {
    return client
        .preparedQuery(CREATE_ADMIN_CONFIG)
        .rxExecute(buildParams(tenantId, adminConfig))
        .map(result -> adminConfig)
        .onErrorResumeNext(
            err ->
                SqlUtils.handleMySqlError(
                    err,
                    ADMIN_CONFIG_ALREADY_EXISTS,
                    String.format("Admin config already exists: %s", tenantId),
                    INTERNAL_SERVER_ERROR));
  }

  public Maybe<AdminConfigModel> getAdminConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_ADMIN_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, AdminConfigModel.class).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateAdminConfig(
      SqlConnection client, String tenantId, AdminConfigModel adminConfig) {
    return client
        .preparedQuery(UPDATE_ADMIN_CONFIG)
        .rxExecute(buildParams(tenantId, adminConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteAdminConfig(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(DELETE_ADMIN_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, AdminConfigModel adminConfig) {
    return Tuple.tuple()
        .addString(adminConfig.getUsername())
        .addString(adminConfig.getPassword())
        .addString(tenantId);
  }
}
