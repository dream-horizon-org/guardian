package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.dreamsportslabs.guardian.constant.ClientAuthMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
  private ClientAuthMethod clientAuthMethod;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("user_identifier")
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

    validateString(issuer, "issuer", 256, false);
    validateString(jwksUrl, "jwks_url", 256, false);
    validateString(tokenUrl, "token_url", 256, false);
    validateString(clientId, "client_id", 256, false);
    validateString(clientSecret, "client_secret", 256, false);
    validateString(redirectUri, "redirect_uri", 256, false);
    validateString(userIdentifier, "user_identifier", 20, false);
  }
}
