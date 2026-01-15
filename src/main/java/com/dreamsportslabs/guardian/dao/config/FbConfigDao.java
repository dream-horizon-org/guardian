package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.CREATE_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.DELETE_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.GET_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.FbConfigQuery.UPDATE_FB_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FB_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.FbConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
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
public class FbConfigDao {
  private final MysqlClient mysqlClient;

  public Single<FbConfigModel> createFbConfig(
      SqlConnection client, String tenantId, FbConfigModel fbConfig) {
    return client
        .preparedQuery(CREATE_FB_CONFIG)
        .rxExecute(buildParams(tenantId, fbConfig))
        .map(result -> fbConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    FB_CONFIG_ALREADY_EXISTS.getCustomException(
                        String.format("FB config already exists: %s", tenantId)));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<FbConfigModel> getFbConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_FB_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result ->
                result.size() == 0
                    ? Maybe.empty()
                    : Maybe.just(JsonUtils.rowSetToList(result, FbConfigModel.class).get(0)))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateFbConfig(SqlConnection client, String tenantId, FbConfigModel fbConfig) {
    return client
        .preparedQuery(UPDATE_FB_CONFIG)
        .rxExecute(buildParams(tenantId, fbConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteFbConfig(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(DELETE_FB_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, FbConfigModel fbConfig) {
    return Tuple.tuple()
        .addString(fbConfig.getAppId())
        .addString(fbConfig.getAppSecret())
        .addValue(fbConfig.getSendAppSecret())
        .addString(tenantId);
  }
}
