package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcIdTokenSigningAlgValue;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.dreamsportslabs.guardian.constant.OidcSubjectType;
import com.dreamsportslabs.guardian.constant.OidcTokenEndpointAuthMethod;
import com.dreamsportslabs.guardian.dao.model.config.OidcConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OidcConfigResponseDto {
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

  public static OidcConfigResponseDto from(String tenantId, OidcConfigModel model) {
    return OidcConfigResponseDto.builder()
        .tenantId(tenantId)
        .issuer(model.getIssuer())
        .authorizationEndpoint(model.getAuthorizationEndpoint())
        .tokenEndpoint(model.getTokenEndpoint())
        .userinfoEndpoint(model.getUserinfoEndpoint())
        .revocationEndpoint(model.getRevocationEndpoint())
        .jwksUri(model.getJwksUri())
        .grantTypesSupported(model.getGrantTypesSupported())
        .responseTypesSupported(model.getResponseTypesSupported())
        .subjectTypesSupported(model.getSubjectTypesSupported())
        .idTokenSigningAlgValuesSupported(model.getIdTokenSigningAlgValuesSupported())
        .tokenEndpointAuthMethodsSupported(model.getTokenEndpointAuthMethodsSupported())
        .loginPageUri(model.getLoginPageUri())
        .consentPageUri(model.getConsentPageUri())
        .authorizeTtl(model.getAuthorizeTtl())
        .build();
  }
}
