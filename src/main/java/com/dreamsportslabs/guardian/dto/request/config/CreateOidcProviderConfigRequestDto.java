package com.dreamsportslabs.guardian.dto.request.config;

import com.dreamsportslabs.guardian.constant.ClientAuthMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Getter;

@Getter
public class CreateOidcProviderConfigRequestDto {
  @JsonProperty("provider_name")
  @NotBlank(message = "provider_name cannot be blank")
  @Size(max = 50, message = "provider_name cannot exceed 50 characters")
  private String providerName;

  @NotBlank(message = "issuer cannot be blank")
  @Size(max = 256, message = "issuer cannot exceed 256 characters")
  private String issuer;

  @JsonProperty("jwks_url")
  @NotBlank(message = "jwks_url cannot be blank")
  @Size(max = 256, message = "jwks_url cannot exceed 256 characters")
  private String jwksUrl;

  @JsonProperty("token_url")
  @NotBlank(message = "token_url cannot be blank")
  @Size(max = 256, message = "token_url cannot exceed 256 characters")
  private String tokenUrl;

  @JsonProperty("client_id")
  @NotBlank(message = "client_id cannot be blank")
  @Size(max = 256, message = "client_id cannot exceed 256 characters")
  private String clientId;

  @JsonProperty("client_secret")
  @NotBlank(message = "client_secret cannot be blank")
  @Size(max = 256, message = "client_secret cannot exceed 256 characters")
  private String clientSecret;

  @JsonProperty("redirect_uri")
  @NotBlank(message = "redirect_uri cannot be blank")
  @Size(max = 256, message = "redirect_uri cannot exceed 256 characters")
  private String redirectUri;

  @JsonProperty("client_auth_method")
  @NotNull(message = "client_auth_method cannot be null")
  private ClientAuthMethod clientAuthMethod;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("user_identifier")
  @Size(max = 20, message = "user_identifier cannot exceed 20 characters")
  private String userIdentifier;

  @JsonProperty("audience_claims")
  @NotNull(message = "audience_claims cannot be null")
  private Map<String, Object> audienceClaims;
}
