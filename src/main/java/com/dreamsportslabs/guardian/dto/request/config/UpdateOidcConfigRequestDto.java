package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.Utils.requireNonBlankIfPresent;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcIdTokenSigningAlgValue;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.dreamsportslabs.guardian.constant.OidcSubjectType;
import com.dreamsportslabs.guardian.constant.OidcTokenEndpointAuthMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

@Getter
public class UpdateOidcConfigRequestDto {
  @Size(max = 255, message = "issuer cannot exceed 255 characters")
  private String issuer;

  @JsonProperty("authorization_endpoint")
  @Size(max = 255, message = "authorization_endpoint cannot exceed 255 characters")
  private String authorizationEndpoint;

  @JsonProperty("token_endpoint")
  @Size(max = 255, message = "token_endpoint cannot exceed 255 characters")
  private String tokenEndpoint;

  @JsonProperty("userinfo_endpoint")
  @Size(max = 255, message = "userinfo_endpoint cannot exceed 255 characters")
  private String userinfoEndpoint;

  @JsonProperty("revocation_endpoint")
  @Size(max = 255, message = "revocation_endpoint cannot exceed 255 characters")
  private String revocationEndpoint;

  @JsonProperty("jwks_uri")
  @Size(max = 255, message = "jwks_uri cannot exceed 255 characters")
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
  @Size(max = 512, message = "login_page_uri cannot exceed 512 characters")
  private String loginPageUri;

  @JsonProperty("consent_page_uri")
  @Size(max = 512, message = "consent_page_uri cannot exceed 512 characters")
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

    requireNonBlankIfPresent(issuer, "issuer");
    requireNonBlankIfPresent(authorizationEndpoint, "authorization_endpoint");
    requireNonBlankIfPresent(tokenEndpoint, "token_endpoint");
    requireNonBlankIfPresent(userinfoEndpoint, "userinfo_endpoint");
    requireNonBlankIfPresent(revocationEndpoint, "revocation_endpoint");
    requireNonBlankIfPresent(jwksUri, "jwks_uri");
  }
}
