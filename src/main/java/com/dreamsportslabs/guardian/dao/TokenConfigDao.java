package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.TokenConfigQuery.CREATE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.TokenConfigQuery.GET_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.TokenConfigQuery.UPDATE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.TokenConfigModel;
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
public class TokenConfigDao {
  private final MysqlClient mysqlClient;

  public Completable createDefaultTokenConfigInTransaction(
      SqlConnection client, TokenConfigModel tokenConfig) {
    return client
        .preparedQuery(CREATE_TOKEN_CONFIG)
        .rxExecute(buildCreateParams(tokenConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Maybe<TokenConfigModel> getTokenConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_TOKEN_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, TokenConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateTokenConfig(TokenConfigModel tokenConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_TOKEN_CONFIG)
        .rxExecute(buildUpdateParams(tokenConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(TokenConfigModel tokenConfig) {
    Tuple params = Tuple.tuple().addString(tokenConfig.getTenantId());
    for (Object v : buildCommonValues(tokenConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(TokenConfigModel tokenConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(tokenConfig)) {
      params.addValue(v);
    }
    params.addString(tokenConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(TokenConfigModel tokenConfig) {
    List<Object> values = new ArrayList<>();
    values.add(tokenConfig.getAlgorithm());
    values.add(tokenConfig.getIssuer());
    values.add(tokenConfig.getRsaKeys());
    values.add(tokenConfig.getAccessTokenExpiry());
    values.add(tokenConfig.getRefreshTokenExpiry());
    values.add(tokenConfig.getIdTokenExpiry());
    values.add(tokenConfig.getIdTokenClaims());
    values.add(tokenConfig.getCookieSameSite());
    values.add(tokenConfig.getCookieDomain());
    values.add(tokenConfig.getCookiePath());
    values.add(tokenConfig.getCookieSecure());
    values.add(tokenConfig.getCookieHttpOnly());
    values.add(tokenConfig.getAccessTokenClaims());
    return values;
  }
}
