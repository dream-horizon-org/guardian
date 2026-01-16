package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.APPLICATION_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_ID_TOKEN_CLAIM_EMAIL_ID;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_ID_TOKEN_CLAIM_USER_ID;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_RSA_KEY_COUNT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_RSA_KEY_SIZE;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_ACCESS_TOKEN_EXPIRY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_ALGORITHM;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_COOKIE_DOMAIN;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_COOKIE_HTTP_ONLY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_COOKIE_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_COOKIE_SAME_SITE;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_COOKIE_SECURE;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_ID_TOKEN_EXPIRY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_REFRESH_TOKEN_EXPIRY;
import static com.dreamsportslabs.guardian.constant.Constants.FIRST_RSA_KEY_INDEX;
import static com.dreamsportslabs.guardian.constant.Constants.FORMAT_PEM;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TOKEN_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;
import static com.dreamsportslabs.guardian.utils.Utils.generateRsaKeys;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.TokenConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.TokenConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTokenConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.dreamsportslabs.guardian.service.RsaKeyPairGeneratorService;
import com.dreamsportslabs.guardian.utils.SharedDataUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TokenConfigService {
  private static final List<String> EMPTY_STRING_LIST = List.of();
  private static final List<String> DEFAULT_ID_TOKEN_CLAIMS_LIST =
      List.of(DEFAULT_ID_TOKEN_CLAIM_USER_ID, DEFAULT_ID_TOKEN_CLAIM_EMAIL_ID);

  private final TokenConfigDao tokenConfigDao;
  private final ChangelogService changelogService;
  private final RsaKeyPairGeneratorService rsaKeyPairGeneratorService;
  private final MysqlClient mysqlClient;
  private final Vertx vertx;
  private final TenantCache tenantCache;

  public Completable createDefaultTokenConfig(SqlConnection client, String tenantId) {
    TokenConfigModel tokenConfigModel = buildDefaultTokenConfig(tenantId);
    return tokenConfigDao
        .createTokenConfig(client, tenantId, tokenConfigModel)
        .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
        .ignoreElement();
  }

  public Single<TokenConfigModel> getTokenConfig(String tenantId) {
    return tokenConfigDao
        .getTokenConfig(tenantId)
        .switchIfEmpty(Single.error(TOKEN_CONFIG_NOT_FOUND.getException()));
  }

  public Single<TokenConfigModel> updateTokenConfig(
      String tenantId, UpdateTokenConfigRequestDto requestDto) {
    return tokenConfigDao
        .getTokenConfig(tenantId)
        .switchIfEmpty(Single.error(TOKEN_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              TokenConfigModel updatedConfig = mergeTokenConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          tokenConfigDao
                              .updateTokenConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_TOKEN_CONFIG,
                                      OPERATION_UPDATE,
                                      oldConfig,
                                      updatedConfig,
                                      tenantId))
                              .andThen(Single.just(updatedConfig))
                              .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
                              .toMaybe())
                  .switchIfEmpty(
                      Single.<TokenConfigModel>error(
                          INTERNAL_SERVER_ERROR.getCustomException(
                              "Failed to update token config")));
            });
  }

  private TokenConfigModel mergeTokenConfig(
      UpdateTokenConfigRequestDto requestDto, TokenConfigModel oldConfig) {
    return TokenConfigModel.builder()
        .algorithm(coalesce(requestDto.getAlgorithm(), oldConfig.getAlgorithm()))
        .issuer(coalesce(requestDto.getIssuer(), oldConfig.getIssuer()))
        .rsaKeys(coalesce(requestDto.getRsaKeys(), oldConfig.getRsaKeys()))
        .accessTokenExpiry(
            coalesce(requestDto.getAccessTokenExpiry(), oldConfig.getAccessTokenExpiry()))
        .refreshTokenExpiry(
            coalesce(requestDto.getRefreshTokenExpiry(), oldConfig.getRefreshTokenExpiry()))
        .idTokenExpiry(coalesce(requestDto.getIdTokenExpiry(), oldConfig.getIdTokenExpiry()))
        .idTokenClaims(coalesce(requestDto.getIdTokenClaims(), oldConfig.getIdTokenClaims()))
        .cookieSameSite(coalesce(requestDto.getCookieSameSite(), oldConfig.getCookieSameSite()))
        .cookieDomain(coalesce(requestDto.getCookieDomain(), oldConfig.getCookieDomain()))
        .cookiePath(coalesce(requestDto.getCookiePath(), oldConfig.getCookiePath()))
        .cookieSecure(coalesce(requestDto.getCookieSecure(), oldConfig.getCookieSecure()))
        .cookieHttpOnly(coalesce(requestDto.getCookieHttpOnly(), oldConfig.getCookieHttpOnly()))
        .accessTokenClaims(
            coalesce(requestDto.getAccessTokenClaims(), oldConfig.getAccessTokenClaims()))
        .build();
  }

  TokenConfigModel buildDefaultTokenConfig(String tenantId) {
    JsonObject config = SharedDataUtils.get(vertx, JsonObject.class, APPLICATION_CONFIG);
    return TokenConfigModel.builder()
        .algorithm(DEFAULT_TOKEN_CONFIG_ALGORITHM)
        .issuer(config.getString("default_token_config_issuer"))
        .rsaKeys(
            generateRsaKeys(
                rsaKeyPairGeneratorService,
                DEFAULT_RSA_KEY_COUNT,
                FIRST_RSA_KEY_INDEX,
                DEFAULT_RSA_KEY_SIZE,
                FORMAT_PEM))
        .accessTokenExpiry(DEFAULT_TOKEN_CONFIG_ACCESS_TOKEN_EXPIRY)
        .refreshTokenExpiry(DEFAULT_TOKEN_CONFIG_REFRESH_TOKEN_EXPIRY)
        .idTokenExpiry(DEFAULT_TOKEN_CONFIG_ID_TOKEN_EXPIRY)
        .idTokenClaims(DEFAULT_ID_TOKEN_CLAIMS_LIST)
        .cookieSameSite(DEFAULT_TOKEN_CONFIG_COOKIE_SAME_SITE)
        .cookieDomain(DEFAULT_TOKEN_CONFIG_COOKIE_DOMAIN)
        .cookiePath(DEFAULT_TOKEN_CONFIG_COOKIE_PATH)
        .cookieSecure(DEFAULT_TOKEN_CONFIG_COOKIE_SECURE)
        .cookieHttpOnly(DEFAULT_TOKEN_CONFIG_COOKIE_HTTP_ONLY)
        .accessTokenClaims(EMPTY_STRING_LIST)
        .build();
  }
}
