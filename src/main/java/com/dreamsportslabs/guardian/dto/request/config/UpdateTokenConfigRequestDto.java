package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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
    boolean hasFields = false;

    if (algorithm != null) {
      hasFields = true;
      if (StringUtils.isBlank(algorithm)) {
        throw INVALID_REQUEST.getCustomException("algorithm cannot be blank");
      }
      if (algorithm.length() > 10) {
        throw INVALID_REQUEST.getCustomException("algorithm cannot exceed 10 characters");
      }
    }

    if (issuer != null) {
      hasFields = true;
      if (StringUtils.isBlank(issuer)) {
        throw INVALID_REQUEST.getCustomException("issuer cannot be blank");
      }
      if (issuer.length() > 256) {
        throw INVALID_REQUEST.getCustomException("issuer cannot exceed 256 characters");
      }
    }

    if (rsaKeys != null) {
      hasFields = true;
    }

    if (accessTokenExpiry != null) {
      hasFields = true;
      if (accessTokenExpiry < 1) {
        throw INVALID_REQUEST.getCustomException("access_token_expiry must be greater than 0");
      }
    }

    if (refreshTokenExpiry != null) {
      hasFields = true;
      if (refreshTokenExpiry < 1) {
        throw INVALID_REQUEST.getCustomException("refresh_token_expiry must be greater than 0");
      }
    }

    if (idTokenExpiry != null) {
      hasFields = true;
      if (idTokenExpiry < 1) {
        throw INVALID_REQUEST.getCustomException("id_token_expiry must be greater than 0");
      }
    }

    if (idTokenClaims != null) {
      hasFields = true;
    }

    if (cookieSameSite != null) {
      hasFields = true;
      if (StringUtils.isBlank(cookieSameSite)) {
        throw INVALID_REQUEST.getCustomException("cookie_same_site cannot be blank");
      }
      if (cookieSameSite.length() > 20) {
        throw INVALID_REQUEST.getCustomException("cookie_same_site cannot exceed 20 characters");
      }
    }

    if (cookieDomain != null) {
      hasFields = true;
      if (cookieDomain.length() > 256) {
        throw INVALID_REQUEST.getCustomException("cookie_domain cannot exceed 256 characters");
      }
    }

    if (cookiePath != null) {
      hasFields = true;
      if (StringUtils.isBlank(cookiePath)) {
        throw INVALID_REQUEST.getCustomException("cookie_path cannot be blank");
      }
      if (cookiePath.length() > 256) {
        throw INVALID_REQUEST.getCustomException("cookie_path cannot exceed 256 characters");
      }
    }

    if (cookieSecure != null) {
      hasFields = true;
    }

    if (cookieHttpOnly != null) {
      hasFields = true;
    }

    if (accessTokenClaims != null) {
      hasFields = true;
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
