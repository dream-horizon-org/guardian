package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_COOKIE_HTTP_ONLY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_COOKIE_SECURE;
import static com.dreamsportslabs.guardian.dao.query.TokenConfigQuery.GET_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.TokenConfigQuery.UPDATE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.TokenConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TokenConfigDao {
  private final MysqlClient mysqlClient;

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

  private Tuple buildUpdateParams(TokenConfigModel tokenConfig) {
    return Tuple.tuple()
        .addString(tokenConfig.getAlgorithm())
        .addString(tokenConfig.getIssuer())
        .addString(tokenConfig.getRsaKeys())
        .addInteger(tokenConfig.getAccessTokenExpiry())
        .addInteger(tokenConfig.getRefreshTokenExpiry())
        .addInteger(tokenConfig.getIdTokenExpiry())
        .addString(tokenConfig.getIdTokenClaims())
        .addString(tokenConfig.getCookieSameSite())
        .addString(tokenConfig.getCookieDomain())
        .addString(tokenConfig.getCookiePath())
        .addBoolean(
            tokenConfig.getCookieSecure() != null
                ? tokenConfig.getCookieSecure()
                : DEFAULT_COOKIE_SECURE)
        .addBoolean(
            tokenConfig.getCookieHttpOnly() != null
                ? tokenConfig.getCookieHttpOnly()
                : DEFAULT_COOKIE_HTTP_ONLY)
        .addString(tokenConfig.getAccessTokenClaims())
        .addString(tokenConfig.getTenantId());
  }
}
