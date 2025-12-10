package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.TokenConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonArray;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class TokenConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  private String algorithm;

  private String issuer;

  @JsonProperty("rsa_keys")
  private List<Object> rsaKeys;

  @JsonProperty("access_token_expiry")
  private Integer accessTokenExpiry;

  @JsonProperty("refresh_token_expiry")
  private Integer refreshTokenExpiry;

  @JsonProperty("id_token_expiry")
  private Integer idTokenExpiry;

  @JsonProperty("id_token_claims")
  private List<String> idTokenClaims;

  @JsonProperty("cookie_same_site")
  private String cookieSameSite;

  @JsonProperty("cookie_domain")
  private String cookieDomain;

  @JsonProperty("cookie_path")
  private String cookiePath;

  @JsonProperty("cookie_secure")
  private Boolean cookieSecure;

  @JsonProperty("cookie_http_only")
  private Boolean cookieHttpOnly;

  @JsonProperty("access_token_claims")
  private List<String> accessTokenClaims;

  public static TokenConfigResponseDto from(TokenConfigModel model) {
    JsonArray rsaKeysArray = new JsonArray(model.getRsaKeys());
    JsonArray idTokenClaimsArray = new JsonArray(model.getIdTokenClaims());
    JsonArray accessTokenClaimsArray = new JsonArray(model.getAccessTokenClaims());

    return TokenConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .algorithm(model.getAlgorithm())
        .issuer(model.getIssuer())
        .rsaKeys(rsaKeysArray.getList())
        .accessTokenExpiry(model.getAccessTokenExpiry())
        .refreshTokenExpiry(model.getRefreshTokenExpiry())
        .idTokenExpiry(model.getIdTokenExpiry())
        .idTokenClaims(
            idTokenClaimsArray.stream()
                .map(item -> item instanceof String ? (String) item : item.toString())
                .toList())
        .cookieSameSite(model.getCookieSameSite())
        .cookieDomain(model.getCookieDomain())
        .cookiePath(model.getCookiePath())
        .cookieSecure(model.getCookieSecure())
        .cookieHttpOnly(model.getCookieHttpOnly())
        .accessTokenClaims(
            accessTokenClaimsArray.stream()
                .map(item -> item instanceof String ? (String) item : item.toString())
                .toList())
        .build();
  }
}
