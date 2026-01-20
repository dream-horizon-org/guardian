package com.dreamsportslabs.guardian.dto.request.config;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcIdTokenSigningAlgValue;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.dreamsportslabs.guardian.constant.OidcSubjectType;
import com.dreamsportslabs.guardian.constant.OidcTokenEndpointAuthMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

@Getter
public class CreateOidcConfigRequestDto {
  @NotBlank(message = "issuer cannot be blank")
  @Size(max = 255, message = "issuer cannot exceed 255 characters")
  private String issuer;

  @JsonProperty("authorization_endpoint")
  @NotBlank(message = "authorization_endpoint cannot be blank")
  @Size(max = 255, message = "authorization_endpoint cannot exceed 255 characters")
  private String authorizationEndpoint;

  @JsonProperty("token_endpoint")
  @NotBlank(message = "token_endpoint cannot be blank")
  @Size(max = 255, message = "token_endpoint cannot exceed 255 characters")
  private String tokenEndpoint;

  @JsonProperty("userinfo_endpoint")
  @NotBlank(message = "userinfo_endpoint cannot be blank")
  @Size(max = 255, message = "userinfo_endpoint cannot exceed 255 characters")
  private String userinfoEndpoint;

  @JsonProperty("revocation_endpoint")
  @NotBlank(message = "revocation_endpoint cannot be blank")
  @Size(max = 255, message = "revocation_endpoint cannot exceed 255 characters")
  private String revocationEndpoint;

  @JsonProperty("jwks_uri")
  @NotBlank(message = "jwks_uri cannot be blank")
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
}
