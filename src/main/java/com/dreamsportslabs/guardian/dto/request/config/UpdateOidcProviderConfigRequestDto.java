package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UpdateOidcProviderConfigRequestDto {
  private String issuer;

  @JsonProperty("jwks_url")
  private String jwksUrl;

  @JsonProperty("token_url")
  private String tokenUrl;

  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("client_secret")
  private String clientSecret;

  @JsonProperty("redirect_uri")
  private String redirectUri;

  @JsonProperty("client_auth_method")
  private String clientAuthMethod;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("user_identifier")
  private String userIdentifier;

  @JsonProperty("audience_claims")
  private java.util.List<String> audienceClaims;

  public void validate() {
    boolean hasFields = false;

    if (issuer != null) {
      hasFields = true;
      if (StringUtils.isBlank(issuer)) {
        throw INVALID_REQUEST.getCustomException("issuer cannot be blank");
      }
    }

    if (jwksUrl != null) {
      hasFields = true;
      if (StringUtils.isBlank(jwksUrl)) {
        throw INVALID_REQUEST.getCustomException("jwks_url cannot be blank");
      }
    }

    if (tokenUrl != null) {
      hasFields = true;
      if (StringUtils.isBlank(tokenUrl)) {
        throw INVALID_REQUEST.getCustomException("token_url cannot be blank");
      }
    }

    if (clientId != null) {
      hasFields = true;
      if (StringUtils.isBlank(clientId)) {
        throw INVALID_REQUEST.getCustomException("client_id cannot be blank");
      }
      if (clientId.length() > 256) {
        throw INVALID_REQUEST.getCustomException("client_id cannot exceed 256 characters");
      }
    }

    if (clientSecret != null) {
      hasFields = true;
      if (StringUtils.isBlank(clientSecret)) {
        throw INVALID_REQUEST.getCustomException("client_secret cannot be blank");
      }
    }

    if (redirectUri != null) {
      hasFields = true;
      if (StringUtils.isBlank(redirectUri)) {
        throw INVALID_REQUEST.getCustomException("redirect_uri cannot be blank");
      }
    }

    if (clientAuthMethod != null) {
      hasFields = true;
      if (StringUtils.isBlank(clientAuthMethod)) {
        throw INVALID_REQUEST.getCustomException("client_auth_method cannot be blank");
      }
      if (clientAuthMethod.length() > 256) {
        throw INVALID_REQUEST.getCustomException("client_auth_method cannot exceed 256 characters");
      }
    }

    if (isSslEnabled != null) {
      hasFields = true;
    }

    if (userIdentifier != null) {
      hasFields = true;
      if (userIdentifier.length() > 20) {
        throw INVALID_REQUEST.getCustomException("user_identifier cannot exceed 20 characters");
      }
    }

    if (audienceClaims != null) {
      hasFields = true;
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
