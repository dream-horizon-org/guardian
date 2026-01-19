package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.dao.config.query.UserConfigQuery.CREATE_USER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.UserConfigQuery.GET_USER_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.UserConfigQuery.UPDATE_USER_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.UserConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
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
public class UserConfigDao {
  private final MysqlClient mysqlClient;

  public Single<UserConfigModel> createUserConfig(
      SqlConnection client, String tenantId, UserConfigModel userConfig) {
    return client
        .preparedQuery(CREATE_USER_CONFIG)
        .rxExecute(buildParams(tenantId, userConfig))
        .map(result -> userConfig)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Maybe<UserConfigModel> getUserConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_USER_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, UserConfigModel.class).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateUserConfig(
      SqlConnection client, String tenantId, UserConfigModel userConfig) {
    return client
        .preparedQuery(UPDATE_USER_CONFIG)
        .rxExecute(buildParams(tenantId, userConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, UserConfigModel userConfig) {
    return Tuple.tuple()
        .addValue(userConfig.getIsSslEnabled())
        .addString(userConfig.getHost())
        .addInteger(userConfig.getPort())
        .addString(userConfig.getGetUserPath())
        .addString(userConfig.getCreateUserPath())
        .addString(userConfig.getAuthenticateUserPath())
        .addString(userConfig.getAddProviderPath())
        .addString(userConfig.getUpdateUserPath())
        .addValue(userConfig.getSendProviderDetails())
        .addString(tenantId);
  }
}
