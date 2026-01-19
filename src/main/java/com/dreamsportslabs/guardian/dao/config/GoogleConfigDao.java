package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.CREATE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.DELETE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.GET_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GoogleConfigQuery.UPDATE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GOOGLE_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.GoogleConfigModel;
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
public class GoogleConfigDao {
  private final MysqlClient mysqlClient;

  public Single<GoogleConfigModel> createGoogleConfig(
      SqlConnection client, String tenantId, GoogleConfigModel googleConfig) {
    return client
        .preparedQuery(CREATE_GOOGLE_CONFIG)
        .rxExecute(buildParams(tenantId, googleConfig))
        .map(result -> googleConfig)
        .onErrorResumeNext(
            err ->
                SqlUtils.handleMySqlError(
                    err,
                    GOOGLE_CONFIG_ALREADY_EXISTS,
                    String.format("Google config already exists: %s", tenantId),
                    INTERNAL_SERVER_ERROR));
  }

  public Maybe<GoogleConfigModel> getGoogleConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_GOOGLE_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, GoogleConfigModel.class).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateGoogleConfig(
      SqlConnection client, String tenantId, GoogleConfigModel googleConfig) {
    return client
        .preparedQuery(UPDATE_GOOGLE_CONFIG)
        .rxExecute(buildParams(tenantId, googleConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteGoogleConfig(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(DELETE_GOOGLE_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, GoogleConfigModel googleConfig) {
    return Tuple.tuple()
        .addString(googleConfig.getClientId())
        .addString(googleConfig.getClientSecret())
        .addString(tenantId);
  }
}
