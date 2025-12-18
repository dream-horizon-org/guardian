package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.OidcConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonArray;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
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

  public static OidcConfigResponseDto from(OidcConfigModel model) {
    return OidcConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .issuer(model.getIssuer())
        .authorizationEndpoint(model.getAuthorizationEndpoint())
        .tokenEndpoint(model.getTokenEndpoint())
        .userinfoEndpoint(model.getUserinfoEndpoint())
        .revocationEndpoint(model.getRevocationEndpoint())
        .jwksUri(model.getJwksUri())
        .grantTypesSupported(parseJsonArray(model.getGrantTypesSupported()))
        .responseTypesSupported(parseJsonArray(model.getResponseTypesSupported()))
        .subjectTypesSupported(parseJsonArray(model.getSubjectTypesSupported()))
        .idTokenSigningAlgValuesSupported(
            parseJsonArray(model.getIdTokenSigningAlgValuesSupported()))
        .tokenEndpointAuthMethodsSupported(
            parseJsonArray(model.getTokenEndpointAuthMethodsSupported()))
        .loginPageUri(model.getLoginPageUri())
        .consentPageUri(model.getConsentPageUri())
        .authorizeTtl(model.getAuthorizeTtl())
        .build();
  }

  private static List<String> parseJsonArray(String jsonString) {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      return List.of();
    }
    try {
      JsonArray jsonArray = new JsonArray(jsonString);
      return jsonArray.stream()
          .map(item -> item instanceof String ? (String) item : item.toString())
          .toList();
    } catch (Exception e) {
      return List.of();
    }
  }
}
