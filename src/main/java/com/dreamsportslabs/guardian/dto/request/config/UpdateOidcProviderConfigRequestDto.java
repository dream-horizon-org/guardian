package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

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
  private List<String> audienceClaims;

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateOidcProviderConfigRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(
        req.getIssuer(),
        req.getJwksUrl(),
        req.getTokenUrl(),
        req.getClientId(),
        req.getClientSecret(),
        req.getRedirectUri(),
        req.getClientAuthMethod(),
        req.getIsSslEnabled(),
        req.getUserIdentifier(),
        req.getAudienceClaims());

    validateString(req.getIssuer(), "issuer", Integer.MAX_VALUE, true);
    validateString(req.getJwksUrl(), "jwks_url", Integer.MAX_VALUE, true);
    validateString(req.getTokenUrl(), "token_url", Integer.MAX_VALUE, true);
    validateString(req.getClientId(), "client_id", 256, true);
    validateString(req.getClientSecret(), "client_secret", Integer.MAX_VALUE, true);
    validateString(req.getRedirectUri(), "redirect_uri", Integer.MAX_VALUE, true);
    validateString(req.getClientAuthMethod(), "client_auth_method", 256, true);
    validateString(req.getUserIdentifier(), "user_identifier", 20, false);
  }
}
