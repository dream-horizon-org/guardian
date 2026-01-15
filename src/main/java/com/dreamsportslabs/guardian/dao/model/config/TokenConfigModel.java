package com.dreamsportslabs.guardian.dao.model.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Setter
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenConfigModel {
  private String algorithm;
  private String issuer;
  private Integer accessTokenExpiry;
  private Integer refreshTokenExpiry;
  private Integer idTokenExpiry;
  private List<String> idTokenClaims;
  private List<RsaKey> rsaKeys;
  private String cookieDomain;
  private String cookieSameSite;
  private String cookiePath;
  private Boolean cookieSecure;
  private Boolean cookieHttpOnly;
  private List<String> accessTokenClaims;
}
