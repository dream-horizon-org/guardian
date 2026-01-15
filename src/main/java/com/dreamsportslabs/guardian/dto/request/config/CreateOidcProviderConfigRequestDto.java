package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireNonNull;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateEnum;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.dreamsportslabs.guardian.constant.ClientAuthMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOidcProviderConfigRequestDto {
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
  private ClientAuthMethod clientAuthMethod;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("user_identifier")
  private String userIdentifier;

  @JsonProperty("audience_claims")
  private Map<String, Object> audienceClaims;

  public void validate() {
    validateString(providerName, "provider_name", 50, true);
    validateString(issuer, "issuer", 256, true);
    validateString(jwksUrl, "jwks_url", 256, true);
    validateString(tokenUrl, "token_url", 256, true);
    validateString(clientId, "client_id", 256, true);
    validateString(clientSecret, "client_secret", 256, true);
    validateString(redirectUri, "redirect_uri", 256, true);
    validateEnum(clientAuthMethod, "client_auth_method", true);
    validateString(userIdentifier, "user_identifier", 20, false);
    requireNonNull(audienceClaims, "audience_claims");
  }
}
