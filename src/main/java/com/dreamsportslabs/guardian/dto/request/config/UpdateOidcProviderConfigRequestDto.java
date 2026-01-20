package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.Utils.requireNonBlankIfPresent;

import com.dreamsportslabs.guardian.constant.ClientAuthMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Getter;

@Getter
public class UpdateOidcProviderConfigRequestDto {
  @Size(max = 256, message = "issuer cannot exceed 256 characters")
  private String issuer;

  @JsonProperty("jwks_url")
  @Size(max = 256, message = "jwks_url cannot exceed 256 characters")
  private String jwksUrl;

  @JsonProperty("token_url")
  @Size(max = 256, message = "token_url cannot exceed 256 characters")
  private String tokenUrl;

  @JsonProperty("client_id")
  @Size(max = 256, message = "client_id cannot exceed 256 characters")
  private String clientId;

  @JsonProperty("client_secret")
  @Size(max = 256, message = "client_secret cannot exceed 256 characters")
  private String clientSecret;

  @JsonProperty("redirect_uri")
  @Size(max = 256, message = "redirect_uri cannot exceed 256 characters")
  private String redirectUri;

  @JsonProperty("client_auth_method")
  private ClientAuthMethod clientAuthMethod;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("user_identifier")
  @Size(max = 20, message = "user_identifier cannot exceed 20 characters")
  private String userIdentifier;

  @JsonProperty("audience_claims")
  private Map<String, Object> audienceClaims;

  public void validate() {
    requireAtLeastOneField(
        issuer,
        jwksUrl,
        tokenUrl,
        clientId,
        clientSecret,
        redirectUri,
        clientAuthMethod,
        isSslEnabled,
        userIdentifier,
        audienceClaims);

    requireNonBlankIfPresent(issuer, "issuer");
    requireNonBlankIfPresent(jwksUrl, "jwks_url");
    requireNonBlankIfPresent(tokenUrl, "token_url");
    requireNonBlankIfPresent(clientId, "client_id");
    requireNonBlankIfPresent(clientSecret, "client_secret");
    requireNonBlankIfPresent(redirectUri, "redirect_uri");
  }
}
