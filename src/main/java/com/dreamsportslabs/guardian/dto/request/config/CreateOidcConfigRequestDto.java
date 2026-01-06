package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequiredString;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class CreateOidcConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

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
    validateRequiredString(tenantId, "tenant_id", 10);
    validateRequiredString(issuer, "issuer", 255);
    validateRequiredString(authorizationEndpoint, "authorization_endpoint", 255);
    validateRequiredString(tokenEndpoint, "token_endpoint", 255);
    validateRequiredString(userinfoEndpoint, "userinfo_endpoint", 255);
    validateRequiredString(revocationEndpoint, "revocation_endpoint", 255);
    validateRequiredString(jwksUri, "jwks_uri", 255);
    validateString(loginPageUri, "login_page_uri", 512, false);
    validateString(consentPageUri, "consent_page_uri", 512, false);
  }
}
