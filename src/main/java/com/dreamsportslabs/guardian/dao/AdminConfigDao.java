package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.query.AdminConfigQuery.CREATE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.AdminConfigQuery.DELETE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.AdminConfigQuery.GET_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.AdminConfigQuery.UPDATE_ADMIN_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.ADMIN_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.AdminConfigModel;
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
public class AdminConfigDao {
  private final MysqlClient mysqlClient;

  public Single<AdminConfigModel> createAdminConfig(AdminConfigModel adminConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_ADMIN_CONFIG)
        .rxExecute(buildCreateParams(adminConfig))
        .map(result -> adminConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    ADMIN_CONFIG_ALREADY_EXISTS.getCustomException(
                        "Admin config already exists: " + adminConfig.getTenantId()));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<AdminConfigModel> getAdminConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_ADMIN_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, AdminConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateAdminConfig(AdminConfigModel adminConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_ADMIN_CONFIG)
        .rxExecute(buildUpdateParams(adminConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteAdminConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_ADMIN_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(AdminConfigModel adminConfig) {
    Tuple params = Tuple.tuple().addString(adminConfig.getTenantId());
    for (Object v : buildCommonValues(adminConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(AdminConfigModel adminConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(adminConfig)) {
      params.addValue(v);
    }
    params.addString(adminConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(AdminConfigModel adminConfig) {
    List<Object> values = new ArrayList<>();
    values.add(adminConfig.getUsername());
    values.add(adminConfig.getPassword());
    return values;
  }
}
