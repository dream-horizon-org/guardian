package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class UpdateOidcConfigRequestDto {
  private String issuer;

  @JsonProperty("authorization_endpoint")
  private String authorizationEndpoint;

  @JsonProperty("token_endpoint")
  private String tokenEndpoint;

  @JsonProperty("userinfo_endpoint")
  private String userinfoEndpoint;

  @JsonProperty("revocation_endpoint")
  private String revocationEndpoint;

  @JsonProperty("jwks_uri")
  private String jwksUri;

  @JsonProperty("grant_types_supported")
  private List<String> grantTypesSupported;

  @JsonProperty("response_types_supported")
  private List<String> responseTypesSupported;

  @JsonProperty("subject_types_supported")
  private List<String> subjectTypesSupported;

  @JsonProperty("id_token_signing_alg_values_supported")
  private List<String> idTokenSigningAlgValuesSupported;

  @JsonProperty("token_endpoint_auth_methods_supported")
  private List<String> tokenEndpointAuthMethodsSupported;

  @JsonProperty("login_page_uri")
  private String loginPageUri;

  @JsonProperty("consent_page_uri")
  private String consentPageUri;

  @JsonProperty("authorize_ttl")
  private Integer authorizeTtl;

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateOidcConfigRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(
        req.getIssuer(),
        req.getAuthorizationEndpoint(),
        req.getTokenEndpoint(),
        req.getUserinfoEndpoint(),
        req.getRevocationEndpoint(),
        req.getJwksUri(),
        req.getGrantTypesSupported(),
        req.getResponseTypesSupported(),
        req.getSubjectTypesSupported(),
        req.getIdTokenSigningAlgValuesSupported(),
        req.getTokenEndpointAuthMethodsSupported(),
        req.getLoginPageUri(),
        req.getConsentPageUri(),
        req.getAuthorizeTtl());

    validateString(req.getIssuer(), "issuer", 255, true);
    validateString(req.getAuthorizationEndpoint(), "authorization_endpoint", 255, true);
    validateString(req.getTokenEndpoint(), "token_endpoint", 255, true);
    validateString(req.getUserinfoEndpoint(), "userinfo_endpoint", 255, true);
    validateString(req.getRevocationEndpoint(), "revocation_endpoint", 255, true);
    validateString(req.getJwksUri(), "jwks_uri", 255, true);
    validateString(req.getLoginPageUri(), "login_page_uri", 512, false);
    validateString(req.getConsentPageUri(), "consent_page_uri", 512, false);
  }
}
