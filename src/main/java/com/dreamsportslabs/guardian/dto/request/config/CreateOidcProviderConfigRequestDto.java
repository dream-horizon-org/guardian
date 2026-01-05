package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateOidcProviderConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("provider_name")
  private String providerName;

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
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot be blank");
    }
    if (tenantId.length() > 10) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot exceed 10 characters");
    }

    if (StringUtils.isBlank(providerName)) {
      throw INVALID_REQUEST.getCustomException("provider_name cannot be blank");
    }
    if (providerName.length() > 50) {
      throw INVALID_REQUEST.getCustomException("provider_name cannot exceed 50 characters");
    }

    if (StringUtils.isBlank(issuer)) {
      throw INVALID_REQUEST.getCustomException("issuer cannot be blank");
    }

    if (StringUtils.isBlank(jwksUrl)) {
      throw INVALID_REQUEST.getCustomException("jwks_url cannot be blank");
    }

    if (StringUtils.isBlank(tokenUrl)) {
      throw INVALID_REQUEST.getCustomException("token_url cannot be blank");
    }

    if (StringUtils.isBlank(clientId)) {
      throw INVALID_REQUEST.getCustomException("client_id cannot be blank");
    }
    if (clientId.length() > 256) {
      throw INVALID_REQUEST.getCustomException("client_id cannot exceed 256 characters");
    }

    if (StringUtils.isBlank(clientSecret)) {
      throw INVALID_REQUEST.getCustomException("client_secret cannot be blank");
    }

    if (StringUtils.isBlank(redirectUri)) {
      throw INVALID_REQUEST.getCustomException("redirect_uri cannot be blank");
    }

    if (StringUtils.isBlank(clientAuthMethod)) {
      throw INVALID_REQUEST.getCustomException("client_auth_method cannot be blank");
    }
    if (clientAuthMethod.length() > 256) {
      throw INVALID_REQUEST.getCustomException("client_auth_method cannot exceed 256 characters");
    }

    if (userIdentifier != null && userIdentifier.length() > 20) {
      throw INVALID_REQUEST.getCustomException("user_identifier cannot exceed 20 characters");
    }

    if (audienceClaims == null) {
      throw INVALID_REQUEST.getCustomException("audience_claims cannot be null");
    }
  }
}
