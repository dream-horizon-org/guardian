package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_SEND_PROVIDER_DETAILS;
import static com.dreamsportslabs.guardian.dao.query.UserConfigQuery.GET_USER_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.UserConfigQuery.UPDATE_USER_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.UserConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserConfigDao {
  private final MysqlClient mysqlClient;

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

  public Completable updateUserConfig(UserConfigModel userConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_USER_CONFIG)
        .rxExecute(buildUpdateParams(userConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildUpdateParams(UserConfigModel userConfig) {
    return Tuple.tuple()
        .addBoolean(
            userConfig.getIsSslEnabled() != null
                ? userConfig.getIsSslEnabled()
                : DEFAULT_IS_SSL_ENABLED)
        .addString(userConfig.getHost())
        .addInteger(userConfig.getPort())
        .addString(userConfig.getGetUserPath())
        .addString(userConfig.getCreateUserPath())
        .addString(userConfig.getAuthenticateUserPath())
        .addString(userConfig.getAddProviderPath())
        .addBoolean(
            userConfig.getSendProviderDetails() != null
                ? userConfig.getSendProviderDetails()
                : DEFAULT_SEND_PROVIDER_DETAILS)
        .addString(userConfig.getTenantId());
  }
}
