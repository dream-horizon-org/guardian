package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.dao.config.query.TokenConfigQuery.CREATE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.TokenConfigQuery.GET_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.TokenConfigQuery.UPDATE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.TokenConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class TokenConfigDao {
  private final MysqlClient mysqlClient;
  private final ObjectMapper objectMapper;

  public Single<TokenConfigModel> createTokenConfig(
      SqlConnection client, String tenantId, TokenConfigModel tokenConfig) {
    return client
        .preparedQuery(CREATE_TOKEN_CONFIG)
        .rxExecute(buildParams(tenantId, tokenConfig))
        .map(result -> tokenConfig)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Maybe<TokenConfigModel> getTokenConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_TOKEN_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result ->
                result.size() == 0
                    ? Maybe.empty()
                    : Maybe.just(JsonUtils.rowSetToList(result, TokenConfigModel.class).get(0)))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateTokenConfig(
      SqlConnection client, String tenantId, TokenConfigModel tokenConfig) {
    return client
        .preparedQuery(UPDATE_TOKEN_CONFIG)
        .rxExecute(buildParams(tenantId, tokenConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildParams(String tenantId, TokenConfigModel tokenConfig) {
    return Tuple.tuple()
        .addString(tokenConfig.getAlgorithm())
        .addString(tokenConfig.getIssuer())
        .addString(
            JsonUtils.serializeToJsonString(
                tokenConfig.getRsaKeys(), JsonUtils.snakeCaseObjectMapper))
        .addInteger(tokenConfig.getAccessTokenExpiry())
        .addInteger(tokenConfig.getRefreshTokenExpiry())
        .addInteger(tokenConfig.getIdTokenExpiry())
        .addString(
            JsonUtils.serializeToJsonString(
                tokenConfig.getIdTokenClaims(), JsonUtils.snakeCaseObjectMapper))
        .addString(tokenConfig.getCookieSameSite())
        .addString(tokenConfig.getCookieDomain())
        .addString(tokenConfig.getCookiePath())
        .addValue(tokenConfig.getCookieSecure())
        .addValue(tokenConfig.getCookieHttpOnly())
        .addString(
            JsonUtils.serializeToJsonString(
                tokenConfig.getAccessTokenClaims(), JsonUtils.snakeCaseObjectMapper))
        .addString(tenantId);
  }
}
