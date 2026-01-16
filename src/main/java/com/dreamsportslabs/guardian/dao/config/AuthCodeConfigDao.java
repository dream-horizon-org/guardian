package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.CREATE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.DELETE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.GET_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.AuthCodeConfigQuery.UPDATE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.AUTH_CODE_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.AuthCodeConfigModel;
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
public class AuthCodeConfigDao {
  private final MysqlClient mysqlClient;

  public Single<AuthCodeConfigModel> createAuthCodeConfig(
      SqlConnection client, String tenantId, AuthCodeConfigModel authCodeConfig) {
    return client
        .preparedQuery(CREATE_AUTH_CODE_CONFIG)
        .rxExecute(buildParams(tenantId, authCodeConfig))
        .map(result -> authCodeConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    AUTH_CODE_CONFIG_ALREADY_EXISTS.getCustomException(
                        String.format("Auth code config already exists: %s", tenantId)));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<AuthCodeConfigModel> getAuthCodeConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_AUTH_CODE_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result ->
                result.size() == 0
                    ? Maybe.empty()
                    : Maybe.just(JsonUtils.rowSetToList(result, AuthCodeConfigModel.class).get(0)))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateAuthCodeConfig(
      SqlConnection client, String tenantId, AuthCodeConfigModel authCodeConfig) {
    return client
        .preparedQuery(UPDATE_AUTH_CODE_CONFIG)
        .rxExecute(buildParams(tenantId, authCodeConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteAuthCodeConfig(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(DELETE_AUTH_CODE_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, AuthCodeConfigModel authCodeConfig) {
    return Tuple.tuple()
        .addInteger(authCodeConfig.getTtl())
        .addInteger(authCodeConfig.getLength())
        .addString(tenantId);
  }
}
