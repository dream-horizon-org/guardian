package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequired;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequiredString;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

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
  private List<String> audienceClaims;

  public void validate() {
    validateRequiredString(tenantId, "tenant_id", 10);
    validateRequiredString(providerName, "provider_name", 50);
    validateRequiredString(issuer, "issuer", Integer.MAX_VALUE);
    validateRequiredString(jwksUrl, "jwks_url", Integer.MAX_VALUE);
    validateRequiredString(tokenUrl, "token_url", Integer.MAX_VALUE);
    validateRequiredString(clientId, "client_id", 256);
    validateRequiredString(clientSecret, "client_secret", Integer.MAX_VALUE);
    validateRequiredString(redirectUri, "redirect_uri", Integer.MAX_VALUE);
    validateRequiredString(clientAuthMethod, "client_auth_method", 256);
    validateString(userIdentifier, "user_identifier", 20, false);
    validateRequired(audienceClaims, "audience_claims");
  }
}
