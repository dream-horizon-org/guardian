package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.query.FbConfigQuery.CREATE_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.FbConfigQuery.DELETE_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.FbConfigQuery.GET_FB_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.FbConfigQuery.UPDATE_FB_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FB_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.FbConfigModel;
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
public class FbConfigDao {
  private final MysqlClient mysqlClient;

  public Single<FbConfigModel> createFbConfig(FbConfigModel fbConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_FB_CONFIG)
        .rxExecute(buildCreateParams(fbConfig))
        .map(result -> fbConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    FB_CONFIG_ALREADY_EXISTS.getCustomException(
                        "FB config already exists: " + fbConfig.getTenantId()));
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
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, FbConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateFbConfig(FbConfigModel fbConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_FB_CONFIG)
        .rxExecute(buildUpdateParams(fbConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteFbConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_FB_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(FbConfigModel fbConfig) {
    Tuple params = Tuple.tuple().addString(fbConfig.getTenantId());
    for (Object v : buildCommonValues(fbConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(FbConfigModel fbConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(fbConfig)) {
      params.addValue(v);
    }
    params.addString(fbConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(FbConfigModel fbConfig) {
    List<Object> values = new ArrayList<>();
    values.add(fbConfig.getAppId());
    values.add(fbConfig.getAppSecret());
    values.add(fbConfig.getSendAppSecret());
    return values;
  }
}
