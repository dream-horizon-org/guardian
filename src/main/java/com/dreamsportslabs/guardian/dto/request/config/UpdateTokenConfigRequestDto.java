package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.Utils.requireNonBlankIfPresent;

import com.dreamsportslabs.guardian.dao.model.config.RsaKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

@Getter
public class UpdateTokenConfigRequestDto {
  @Size(max = 10, message = "algorithm cannot exceed 10 characters")
  private String algorithm;

  @Size(max = 256, message = "issuer cannot exceed 256 characters")
  private String issuer;

  @JsonProperty("rsa_keys")
  private List<RsaKey> rsaKeys;

  @JsonProperty("access_token_expiry")
  @Min(value = 1, message = "access_token_expiry must be greater than or equal to 1")
  private Integer accessTokenExpiry;

  @JsonProperty("refresh_token_expiry")
  @Min(value = 1, message = "refresh_token_expiry must be greater than or equal to 1")
  private Integer refreshTokenExpiry;

  @JsonProperty("id_token_expiry")
  @Min(value = 1, message = "id_token_expiry must be greater than or equal to 1")
  private Integer idTokenExpiry;

  @JsonProperty("id_token_claims")
  private List<String> idTokenClaims;

  @JsonProperty("cookie_same_site")
  @Size(max = 20, message = "cookie_same_site cannot exceed 20 characters")
  private String cookieSameSite;

  @JsonProperty("cookie_domain")
  @Size(max = 256, message = "cookie_domain cannot exceed 256 characters")
  private String cookieDomain;

  @JsonProperty("cookie_path")
  @Size(max = 256, message = "cookie_path cannot exceed 256 characters")
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

    requireNonBlankIfPresent(algorithm, "algorithm");
    requireNonBlankIfPresent(issuer, "issuer");
    requireNonBlankIfPresent(cookieSameSite, "cookie_same_site");
    requireNonBlankIfPresent(cookieDomain, "cookie_domain");
    requireNonBlankIfPresent(cookiePath, "cookie_path");
  }
}
