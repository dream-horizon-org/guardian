package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.query.GoogleConfigQuery.CREATE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.GoogleConfigQuery.DELETE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.GoogleConfigQuery.GET_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.GoogleConfigQuery.UPDATE_GOOGLE_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GOOGLE_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.GoogleConfigModel;
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
public class GoogleConfigDao {
  private final MysqlClient mysqlClient;

  public Single<GoogleConfigModel> createGoogleConfig(GoogleConfigModel googleConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_GOOGLE_CONFIG)
        .rxExecute(buildCreateParams(googleConfig))
        .map(result -> googleConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    GOOGLE_CONFIG_ALREADY_EXISTS.getCustomException(
                        "Google config already exists: " + googleConfig.getTenantId()));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<GoogleConfigModel> getGoogleConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_GOOGLE_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, GoogleConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateGoogleConfig(GoogleConfigModel googleConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_GOOGLE_CONFIG)
        .rxExecute(buildUpdateParams(googleConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteGoogleConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_GOOGLE_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(GoogleConfigModel googleConfig) {
    Tuple params = Tuple.tuple().addString(googleConfig.getTenantId());
    for (Object v : buildCommonValues(googleConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(GoogleConfigModel googleConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(googleConfig)) {
      params.addValue(v);
    }
    params.addString(googleConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(GoogleConfigModel googleConfig) {
    List<Object> values = new ArrayList<>();
    values.add(googleConfig.getClientId());
    values.add(googleConfig.getClientSecret());
    return values;
  }
}
