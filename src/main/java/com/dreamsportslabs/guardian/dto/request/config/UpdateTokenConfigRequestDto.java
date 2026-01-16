package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateInteger;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.dreamsportslabs.guardian.dao.model.config.RsaKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class UpdateTokenConfigRequestDto {
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

  public void validate() {
    requireAtLeastOneField(
        algorithm,
        issuer,
        rsaKeys,
        accessTokenExpiry,
        refreshTokenExpiry,
        idTokenExpiry,
        idTokenClaims,
        cookieSameSite,
        cookieDomain,
        cookiePath,
        cookieSecure,
        cookieHttpOnly,
        accessTokenClaims);

    validateString(algorithm, "algorithm", 10, false);
    validateString(issuer, "issuer", 256, false);
    validateString(cookieSameSite, "cookie_same_site", 20, false);
    validateString(cookieDomain, "cookie_domain", 256, false);
    validateString(cookiePath, "cookie_path", 256, false);
    validateInteger(accessTokenExpiry, "access_token_expiry", 1, false);
    validateInteger(refreshTokenExpiry, "refresh_token_expiry", 1, false);
    validateInteger(idTokenExpiry, "id_token_expiry", 1, false);
  }
}
