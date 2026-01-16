package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcIdTokenSigningAlgValue;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.dreamsportslabs.guardian.constant.OidcSubjectType;
import com.dreamsportslabs.guardian.constant.OidcTokenEndpointAuthMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
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
  private List<OidcGrantType> grantTypesSupported;

  @JsonProperty("response_types_supported")
  private List<OidcResponseType> responseTypesSupported;

  @JsonProperty("subject_types_supported")
  private List<OidcSubjectType> subjectTypesSupported;

  @JsonProperty("id_token_signing_alg_values_supported")
  private List<OidcIdTokenSigningAlgValue> idTokenSigningAlgValuesSupported;

  @JsonProperty("token_endpoint_auth_methods_supported")
  private List<OidcTokenEndpointAuthMethod> tokenEndpointAuthMethodsSupported;

  @JsonProperty("login_page_uri")
  private String loginPageUri;

  @JsonProperty("consent_page_uri")
  private String consentPageUri;

  @JsonProperty("authorize_ttl")
  private Integer authorizeTtl;

  public void validate() {
    requireAtLeastOneField(
        issuer,
        authorizationEndpoint,
        tokenEndpoint,
        userinfoEndpoint,
        revocationEndpoint,
        jwksUri,
        grantTypesSupported,
        responseTypesSupported,
        subjectTypesSupported,
        idTokenSigningAlgValuesSupported,
        tokenEndpointAuthMethodsSupported,
        loginPageUri,
        consentPageUri,
        authorizeTtl);

    validateString(issuer, "issuer", 255, false);
    validateString(authorizationEndpoint, "authorization_endpoint", 255, false);
    validateString(tokenEndpoint, "token_endpoint", 255, false);
    validateString(userinfoEndpoint, "userinfo_endpoint", 255, false);
    validateString(revocationEndpoint, "revocation_endpoint", 255, false);
    validateString(jwksUri, "jwks_uri", 255, false);
    validateString(loginPageUri, "login_page_uri", 512, false);
    validateString(consentPageUri, "consent_page_uri", 512, false);
  }
}
