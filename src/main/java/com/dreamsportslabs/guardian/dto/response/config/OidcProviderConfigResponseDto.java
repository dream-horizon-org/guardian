package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.OidcProviderConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonArray;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class OidcProviderConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

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
  private String clientAuthMethod;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("user_identifier")
  private String userIdentifier;

  @JsonProperty("audience_claims")
  private List<String> audienceClaims;

  public static OidcProviderConfigResponseDto from(OidcProviderConfigModel model) {
    JsonArray audienceClaimsArray = new JsonArray(model.getAudienceClaims());
    List<String> audienceClaimsList =
        audienceClaimsArray.stream()
            .map(item -> item instanceof String ? (String) item : item.toString())
            .toList();

    return OidcProviderConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .providerName(model.getProviderName())
        .issuer(model.getIssuer())
        .jwksUrl(model.getJwksUrl())
        .tokenUrl(model.getTokenUrl())
        .clientId(model.getClientId())
        .clientSecret(model.getClientSecret())
        .redirectUri(model.getRedirectUri())
        .clientAuthMethod(model.getClientAuthMethod())
        .isSslEnabled(model.getIsSslEnabled())
        .userIdentifier(model.getUserIdentifier())
        .audienceClaims(audienceClaimsList)
        .build();
  }
}
