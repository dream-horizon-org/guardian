package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.CREATE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.DELETE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.GET_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.GuestConfigQuery.UPDATE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GUEST_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.GuestConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class GuestConfigDao {
  private final MysqlClient mysqlClient;
  private final ObjectMapper objectMapper;

  public Single<GuestConfigModel> createGuestConfig(
      SqlConnection client, String tenantId, GuestConfigModel guestConfig) {
    return client
        .preparedQuery(CREATE_GUEST_CONFIG)
        .rxExecute(buildParams(tenantId, guestConfig))
        .map(result -> guestConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    GUEST_CONFIG_ALREADY_EXISTS.getCustomException(
                        String.format("Guest config already exists: %s", tenantId)));
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
            result ->
                result.size() == 0
                    ? Maybe.empty()
                    : Maybe.just(JsonUtils.rowSetToList(result, GuestConfigModel.class).get(0)))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateGuestConfig(
      SqlConnection client, String tenantId, GuestConfigModel guestConfig) {
    return client
        .preparedQuery(UPDATE_GUEST_CONFIG)
        .rxExecute(buildParams(tenantId, guestConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteGuestConfig(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(DELETE_GUEST_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, GuestConfigModel guestConfig) {
    return Tuple.tuple()
        .addValue(guestConfig.getIsEncrypted())
        .addString(guestConfig.getSecretKey())
        .addString(JsonUtils.serializeToJsonString(guestConfig.getAllowedScopes(), objectMapper))
        .addString(tenantId);
  }
}
