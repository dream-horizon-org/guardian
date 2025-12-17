package com.dreamsportslabs.guardian.service;

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
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_ISSUER;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_REFRESH_TOKEN_EXPIRY;
import static com.dreamsportslabs.guardian.constant.Constants.FIRST_RSA_KEY_INDEX;
import static com.dreamsportslabs.guardian.constant.Constants.FORMAT_PEM;
import static com.dreamsportslabs.guardian.constant.Constants.JSON_FIELD_CURRENT;
import static com.dreamsportslabs.guardian.constant.Constants.JSON_FIELD_KID;
import static com.dreamsportslabs.guardian.constant.Constants.JSON_FIELD_PRIVATE_KEY;
import static com.dreamsportslabs.guardian.constant.Constants.JSON_FIELD_PUBLIC_KEY;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TOKEN_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;
import static com.dreamsportslabs.guardian.utils.Utils.generateRsaKeysArray;

import com.dreamsportslabs.guardian.dao.TokenConfigDao;
import com.dreamsportslabs.guardian.dao.model.TokenConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTokenConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TokenConfigService {
  private static final String EMPTY_JSON_ARRAY = new JsonArray().encode();
  private static final JsonArray DEFAULT_ID_TOKEN_CLAIMS_ARRAY =
      new JsonArray().add(DEFAULT_ID_TOKEN_CLAIM_USER_ID).add(DEFAULT_ID_TOKEN_CLAIM_EMAIL_ID);

  private final TokenConfigDao tokenConfigDao;
  private final ChangelogService changelogService;
  private final RsaKeyPairGeneratorService rsaKeyPairGeneratorService;

  public Completable createDefaultTokenConfigInTransaction(SqlConnection client, String tenantId) {
    TokenConfigModel tokenConfig = buildDefaultTokenConfig(tenantId);
    return tokenConfigDao.createDefaultTokenConfigInTransaction(client, tokenConfig);
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
              TokenConfigModel updatedConfig = mergeTokenConfig(tenantId, requestDto, oldConfig);
              return tokenConfigDao
                  .updateTokenConfig(updatedConfig)
                  .andThen(getTokenConfig(tenantId))
                  .flatMap(
                      newConfig ->
                          changelogService
                              .logConfigChange(
                                  tenantId,
                                  CONFIG_TYPE_TOKEN_CONFIG,
                                  OPERATION_UPDATE,
                                  oldConfig,
                                  newConfig,
                                  tenantId)
                              .andThen(Single.just(newConfig)));
            });
  }

  private TokenConfigModel mergeTokenConfig(
      String tenantId, UpdateTokenConfigRequestDto requestDto, TokenConfigModel oldConfig) {
    return TokenConfigModel.builder()
        .tenantId(tenantId)
        .algorithm(coalesce(requestDto.getAlgorithm(), oldConfig.getAlgorithm()))
        .issuer(coalesce(requestDto.getIssuer(), oldConfig.getIssuer()))
        .rsaKeys(
            requestDto.getRsaKeys() != null
                ? encodeJsonArray(requestDto.getRsaKeys())
                : oldConfig.getRsaKeys())
        .accessTokenExpiry(
            coalesce(requestDto.getAccessTokenExpiry(), oldConfig.getAccessTokenExpiry()))
        .refreshTokenExpiry(
            coalesce(requestDto.getRefreshTokenExpiry(), oldConfig.getRefreshTokenExpiry()))
        .idTokenExpiry(coalesce(requestDto.getIdTokenExpiry(), oldConfig.getIdTokenExpiry()))
        .idTokenClaims(
            requestDto.getIdTokenClaims() != null
                ? encodeJsonArray(requestDto.getIdTokenClaims())
                : oldConfig.getIdTokenClaims())
        .cookieSameSite(coalesce(requestDto.getCookieSameSite(), oldConfig.getCookieSameSite()))
        .cookieDomain(coalesce(requestDto.getCookieDomain(), oldConfig.getCookieDomain()))
        .cookiePath(coalesce(requestDto.getCookiePath(), oldConfig.getCookiePath()))
        .cookieSecure(coalesce(requestDto.getCookieSecure(), oldConfig.getCookieSecure()))
        .cookieHttpOnly(coalesce(requestDto.getCookieHttpOnly(), oldConfig.getCookieHttpOnly()))
        .accessTokenClaims(
            requestDto.getAccessTokenClaims() != null
                ? encodeJsonArray(requestDto.getAccessTokenClaims())
                : oldConfig.getAccessTokenClaims())
        .build();
  }

  TokenConfigModel buildDefaultTokenConfig(String tenantId) {
    return TokenConfigModel.builder()
        .tenantId(tenantId)
        .algorithm(DEFAULT_TOKEN_CONFIG_ALGORITHM)
        .issuer(DEFAULT_TOKEN_CONFIG_ISSUER)
        .rsaKeys(
            generateRsaKeysArray(
                    rsaKeyPairGeneratorService,
                    DEFAULT_RSA_KEY_COUNT,
                    FIRST_RSA_KEY_INDEX,
                    DEFAULT_RSA_KEY_SIZE,
                    FORMAT_PEM,
                    JSON_FIELD_KID,
                    JSON_FIELD_PUBLIC_KEY,
                    JSON_FIELD_PRIVATE_KEY,
                    JSON_FIELD_CURRENT)
                .encode())
        .accessTokenExpiry(DEFAULT_TOKEN_CONFIG_ACCESS_TOKEN_EXPIRY)
        .refreshTokenExpiry(DEFAULT_TOKEN_CONFIG_REFRESH_TOKEN_EXPIRY)
        .idTokenExpiry(DEFAULT_TOKEN_CONFIG_ID_TOKEN_EXPIRY)
        .idTokenClaims(buildDefaultIdTokenClaims().encode())
        .cookieSameSite(DEFAULT_TOKEN_CONFIG_COOKIE_SAME_SITE)
        .cookieDomain(DEFAULT_TOKEN_CONFIG_COOKIE_DOMAIN)
        .cookiePath(DEFAULT_TOKEN_CONFIG_COOKIE_PATH)
        .cookieSecure(DEFAULT_TOKEN_CONFIG_COOKIE_SECURE)
        .cookieHttpOnly(DEFAULT_TOKEN_CONFIG_COOKIE_HTTP_ONLY)
        .accessTokenClaims(EMPTY_JSON_ARRAY)
        .build();
  }

  private JsonArray buildDefaultIdTokenClaims() {
    return DEFAULT_ID_TOKEN_CLAIMS_ARRAY.copy();
  }

  private String encodeJsonArray(java.util.List<?> list) {
    return new JsonArray(list).encode();
  }
}
