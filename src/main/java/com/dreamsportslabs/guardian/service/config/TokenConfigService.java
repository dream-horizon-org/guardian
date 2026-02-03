package com.dreamsportslabs.guardian.service.config;

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
import static com.dreamsportslabs.guardian.dao.config.query.TokenConfigQuery.CREATE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.TokenConfigQuery.GET_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.TokenConfigQuery.UPDATE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TOKEN_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;
import static com.dreamsportslabs.guardian.utils.Utils.generateRsaKeys;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.TokenConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTokenConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.dreamsportslabs.guardian.service.RsaKeyPairGeneratorService;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenConfigService
    extends BaseConfigService<TokenConfigModel, TokenConfigModel, UpdateTokenConfigRequestDto> {
  private static final List<String> EMPTY_STRING_LIST = List.of();
  private static final List<String> DEFAULT_ID_TOKEN_CLAIMS_LIST =
      List.of(DEFAULT_ID_TOKEN_CLAIM_USER_ID, DEFAULT_ID_TOKEN_CLAIM_EMAIL_ID);

  private final RsaKeyPairGeneratorService rsaKeyPairGeneratorService;

  @Inject
  public TokenConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      RsaKeyPairGeneratorService rsaKeyPairGeneratorService) {
    super(changelogService, mysqlClient, tenantCache);
    this.rsaKeyPairGeneratorService = rsaKeyPairGeneratorService;
  }

  public Completable createDefaultTokenConfig(SqlConnection client, String tenantId) {
    TokenConfigModel tokenConfigModel = buildDefaultTokenConfig(tenantId);
    return getDao().createConfig(client, tenantId, tokenConfigModel).ignoreElement();
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_TOKEN_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_TOKEN_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_TOKEN_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return "";
  }

  @Override
  protected Tuple buildParams(String tenantId, TokenConfigModel tokenConfig) {
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

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return INTERNAL_SERVER_ERROR;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return "Token config already exists";
  }

  @Override
  protected Class<TokenConfigModel> getModelClass() {
    return TokenConfigModel.class;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_TOKEN_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return TOKEN_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create token config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update token config";
  }

  @Override
  protected TokenConfigModel mapToModel(TokenConfigModel requestDto) {
    return requestDto;
  }

  @Override
  protected TokenConfigModel mergeModel(
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

  public Single<TokenConfigModel> getTokenConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<TokenConfigModel> updateTokenConfig(
      String tenantId, UpdateTokenConfigRequestDto requestDto) {
    return updateConfig(tenantId, requestDto);
  }

  TokenConfigModel buildDefaultTokenConfig(String tenantId) {
    return TokenConfigModel.builder()
        .algorithm(DEFAULT_TOKEN_CONFIG_ALGORITHM)
        .issuer(DEFAULT_TOKEN_CONFIG_ISSUER)
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
