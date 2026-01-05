package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.query.GuestConfigQuery.CREATE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.GuestConfigQuery.DELETE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.GuestConfigQuery.GET_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.GuestConfigQuery.UPDATE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GUEST_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.GuestConfigModel;
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
public class GuestConfigDao {
  private final MysqlClient mysqlClient;

  public Single<GuestConfigModel> createGuestConfig(GuestConfigModel guestConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_GUEST_CONFIG)
        .rxExecute(buildCreateParams(guestConfig))
        .map(result -> guestConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    GUEST_CONFIG_ALREADY_EXISTS.getCustomException(
                        "Guest config already exists: " + guestConfig.getTenantId()));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<GuestConfigModel> getGuestConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_GUEST_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, GuestConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateGuestConfig(GuestConfigModel guestConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_GUEST_CONFIG)
        .rxExecute(buildUpdateParams(guestConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteGuestConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_GUEST_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(GuestConfigModel guestConfig) {
    Tuple params = Tuple.tuple().addString(guestConfig.getTenantId());
    for (Object v : buildCommonValues(guestConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(GuestConfigModel guestConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(guestConfig)) {
      params.addValue(v);
    }
    params.addString(guestConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(GuestConfigModel guestConfig) {
    List<Object> values = new ArrayList<>();
    values.add(guestConfig.getIsEncrypted());
    values.add(guestConfig.getSecretKey());
    values.add(guestConfig.getAllowedScopes());
    return values;
  }
}
