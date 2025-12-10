package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TOKEN_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.TokenConfigDao;
import com.dreamsportslabs.guardian.dao.model.TokenConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTokenConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TokenConfigService {
  private final TokenConfigDao tokenConfigDao;
  private final ChangelogService changelogService;

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

  private String encodeJsonArray(java.util.List<?> list) {
    return new JsonArray(list).encode();
  }
}
