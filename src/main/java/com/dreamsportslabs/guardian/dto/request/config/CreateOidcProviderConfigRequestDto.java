package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OIDC_PROVIDER_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_IDENTIFIER;

import com.dreamsportslabs.guardian.constant.ClientAuthMethod;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateOidcProviderConfigRequestDto {
  @NotBlank(message = "provider_name cannot be blank")
  @Size(max = 50, message = "provider_name cannot exceed 50 characters")
  private String providerName;

  @NotNull(message = "issuer cannot be null")
  @Size(max = 256, message = "issuer cannot exceed 256 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "issuer must be a valid HTTP/HTTPS URL")
  private String issuer;

  @NotNull(message = "jwks_url cannot be null")
  @Size(max = 256, message = "jwks_url cannot exceed 256 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "jwks_url must be a valid HTTP/HTTPS URL")
  private String jwksUrl;

  @NotNull(message = "token_url cannot be null")
  @Size(max = 256, message = "token_url cannot exceed 256 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "token_url must be a valid HTTP/HTTPS URL")
  private String tokenUrl;

  @NotBlank(message = "client_id cannot be blank")
  @Size(max = 256, message = "client_id cannot exceed 256 characters")
  private String clientId;

  @NotBlank(message = "client_secret cannot be blank")
  @Size(max = 256, message = "client_secret cannot exceed 256 characters")
  private String clientSecret;

  @NotNull(message = "redirect_uri cannot be null")
  @Size(max = 256, message = "redirect_uri cannot exceed 256 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "redirect_uri must be a valid HTTP/HTTPS URL")
  private String redirectUri;

  @NotNull(message = "client_auth_method cannot be null")
  private ClientAuthMethod clientAuthMethod;

  @NotNull private Boolean isSslEnabled = DEFAULT_OIDC_PROVIDER_IS_SSL_ENABLED;

  @NotNull
  @Size(max = 20, message = "user_identifier cannot exceed 20 characters")
  private String userIdentifier = DEFAULT_USER_IDENTIFIER;

  @NotNull(message = "audience_claims cannot be null")
  private Map<String, Object> audienceClaims;
}
