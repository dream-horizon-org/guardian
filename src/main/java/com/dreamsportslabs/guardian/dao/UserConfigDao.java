package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.UserConfigQuery.CREATE_USER_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.UserConfigQuery.DELETE_USER_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.UserConfigQuery.GET_USER_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.UserConfigQuery.UPDATE_USER_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.USER_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.utils.SqlUtils.prepareUpdateQuery;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.UserConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.UpdateUserConfigRequestDto;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserConfigDao {
  private final MysqlClient mysqlClient;

  public Single<UserConfigModel> createUserConfig(UserConfigModel userConfig) {
    Tuple params =
        Tuple.tuple()
            .addString(userConfig.getTenantId())
            .addBoolean(userConfig.getIsSslEnabled() != null ? userConfig.getIsSslEnabled() : false)
            .addString(userConfig.getHost())
            .addInteger(userConfig.getPort() != null ? userConfig.getPort() : 80)
            .addString(userConfig.getGetUserPath())
            .addString(userConfig.getCreateUserPath())
            .addString(userConfig.getAuthenticateUserPath())
            .addString(userConfig.getAddProviderPath())
            .addBoolean(
                userConfig.getSendProviderDetails() != null
                    ? userConfig.getSendProviderDetails()
                    : false);
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_USER_CONFIG)
        .rxExecute(params)
        .map(result -> userConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == 1062) {
                return Single.error(
                    USER_CONFIG_ALREADY_EXISTS.getCustomException(
                        "User config already exists for tenant: " + userConfig.getTenantId()));
              }

              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<UserConfigModel> getUserConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_USER_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, UserConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateUserConfig(String tenantId, UpdateUserConfigRequestDto updateRequest) {
    Pair<String, Tuple> queryAndTuple = prepareUpdateQuery(updateRequest);
    Tuple tuple = queryAndTuple.getRight().addString(tenantId);
    String query = UPDATE_USER_CONFIG.replace("<<insert_attributes>>", queryAndTuple.getLeft());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(query)
        .rxExecute(tuple)
        .ignoreElement()
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == 1062) {
                return Completable.error(
                    USER_CONFIG_ALREADY_EXISTS.getCustomException(
                        "User config already exists for tenant"));
              }

              return Completable.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Single<Boolean> deleteUserConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_USER_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}

