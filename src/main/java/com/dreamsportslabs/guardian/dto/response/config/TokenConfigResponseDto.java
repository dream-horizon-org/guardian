package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.RsaKey;
import com.dreamsportslabs.guardian.dao.model.config.TokenConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  private String algorithm;

  private String issuer;

  @JsonProperty("rsa_keys")
  private List<RsaKey> rsaKeys;

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

  public static TokenConfigResponseDto from(String tenantId, TokenConfigModel model) {
    return TokenConfigResponseDto.builder()
        .tenantId(tenantId)
        .algorithm(model.getAlgorithm())
        .issuer(model.getIssuer())
        .rsaKeys(model.getRsaKeys())
        .accessTokenExpiry(model.getAccessTokenExpiry())
        .refreshTokenExpiry(model.getRefreshTokenExpiry())
        .idTokenExpiry(model.getIdTokenExpiry())
        .idTokenClaims(model.getIdTokenClaims())
        .cookieSameSite(model.getCookieSameSite())
        .cookieDomain(model.getCookieDomain())
        .cookiePath(model.getCookiePath())
        .cookieSecure(model.getCookieSecure())
        .cookieHttpOnly(model.getCookieHttpOnly())
        .accessTokenClaims(model.getAccessTokenClaims())
        .build();
  }
}
