package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.query.AuthCodeConfigQuery.CREATE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.AuthCodeConfigQuery.DELETE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.AuthCodeConfigQuery.GET_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.AuthCodeConfigQuery.UPDATE_AUTH_CODE_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.AUTH_CODE_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.AuthCodeConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AuthCodeConfigDao {
  private final MysqlClient mysqlClient;

  public Single<AuthCodeConfigModel> createAuthCodeConfig(AuthCodeConfigModel authCodeConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_AUTH_CODE_CONFIG)
        .rxExecute(buildCreateParams(authCodeConfig))
        .map(result -> authCodeConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    AUTH_CODE_CONFIG_ALREADY_EXISTS.getCustomException(
                        "Auth code config already exists: " + authCodeConfig.getTenantId()));
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
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, AuthCodeConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateAuthCodeConfig(AuthCodeConfigModel authCodeConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_AUTH_CODE_CONFIG)
        .rxExecute(buildUpdateParams(authCodeConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteAuthCodeConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_AUTH_CODE_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(AuthCodeConfigModel authCodeConfig) {
    Tuple params = Tuple.tuple().addString(authCodeConfig.getTenantId());
    for (Object v : buildCommonValues(authCodeConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(AuthCodeConfigModel authCodeConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(authCodeConfig)) {
      params.addValue(v);
    }
    params.addString(authCodeConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(AuthCodeConfigModel authCodeConfig) {
    List<Object> values = new ArrayList<>();
    values.add(authCodeConfig.getTtl());
    values.add(authCodeConfig.getLength());
    return values;
  }
}
