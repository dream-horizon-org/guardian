package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.UserConfigQuery.CREATE_USER_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.UserConfigQuery.GET_USER_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.UserConfigQuery.UPDATE_USER_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.UserConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserConfigDao {
  private final MysqlClient mysqlClient;

  public Completable createDefaultUserConfigInTransaction(
      SqlConnection client, UserConfigModel userConfig) {
    return client
        .preparedQuery(CREATE_USER_CONFIG)
        .rxExecute(buildCreateParams(userConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
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

  public Completable updateUserConfig(UserConfigModel userConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_USER_CONFIG)
        .rxExecute(buildUpdateParams(userConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(UserConfigModel userConfig) {
    Tuple params = Tuple.tuple().addString(userConfig.getTenantId());
    for (Object v : buildCommonValues(userConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(UserConfigModel userConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(userConfig)) {
      params.addValue(v);
    }
    params.addString(userConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(UserConfigModel userConfig) {
    List<Object> values = new ArrayList<>();
    values.add(userConfig.getIsSslEnabled());
    values.add(userConfig.getHost());
    values.add(userConfig.getPort());
    values.add(userConfig.getGetUserPath());
    values.add(userConfig.getCreateUserPath());
    values.add(userConfig.getAuthenticateUserPath());
    values.add(userConfig.getAddProviderPath());
    values.add(userConfig.getSendProviderDetails());
    return values;
  }
}
