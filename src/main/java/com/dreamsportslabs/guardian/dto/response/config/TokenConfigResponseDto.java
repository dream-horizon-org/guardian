package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.RsaKey;
import com.dreamsportslabs.guardian.dao.model.config.TokenConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TokenConfigResponseDto {
  private String tenantId;
  private String algorithm;
  private String issuer;
  private List<RsaKey> rsaKeys;
  private Integer accessTokenExpiry;
  private Integer refreshTokenExpiry;
  private Integer idTokenExpiry;
  private List<String> idTokenClaims;
  private String cookieSameSite;
  private String cookieDomain;
  private String cookiePath;
  private Boolean cookieSecure;
  private Boolean cookieHttpOnly;
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
