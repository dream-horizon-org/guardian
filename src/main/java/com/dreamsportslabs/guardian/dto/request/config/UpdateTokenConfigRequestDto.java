package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateInteger;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class UpdateTokenConfigRequestDto {
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

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateTokenConfigRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(
        req.getAlgorithm(),
        req.getIssuer(),
        req.getRsaKeys(),
        req.getAccessTokenExpiry(),
        req.getRefreshTokenExpiry(),
        req.getIdTokenExpiry(),
        req.getIdTokenClaims(),
        req.getCookieSameSite(),
        req.getCookieDomain(),
        req.getCookiePath(),
        req.getCookieSecure(),
        req.getCookieHttpOnly(),
        req.getAccessTokenClaims());

    validateString(req.getAlgorithm(), "algorithm", 10, true);
    validateString(req.getIssuer(), "issuer", 256, true);
    validateString(req.getCookieSameSite(), "cookie_same_site", 20, true);
    validateString(req.getCookieDomain(), "cookie_domain", 256, false);
    validateString(req.getCookiePath(), "cookie_path", 256, true);

    validateInteger(req.getAccessTokenExpiry(), "access_token_expiry", 1);
    validateInteger(req.getRefreshTokenExpiry(), "refresh_token_expiry", 1);
    validateInteger(req.getIdTokenExpiry(), "id_token_expiry", 1);
  }
}
