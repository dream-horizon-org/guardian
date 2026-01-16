package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.constant.ClientAuthMethod;
import com.dreamsportslabs.guardian.dao.model.config.OidcProviderConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

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
  private ClientAuthMethod clientAuthMethod;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("user_identifier")
  private String userIdentifier;

  @JsonProperty("audience_claims")
  private Map<String, Object> audienceClaims;

  public static OidcProviderConfigResponseDto from(
      String tenantId, String providerName, OidcProviderConfigModel model) {
    return OidcProviderConfigResponseDto.builder()
        .tenantId(tenantId)
        .providerName(providerName)
        .issuer(model.getIssuer())
        .jwksUrl(model.getJwksUrl())
        .tokenUrl(model.getTokenUrl())
        .clientId(model.getClientId())
        .clientSecret(model.getClientSecret())
        .redirectUri(model.getRedirectUri())
        .clientAuthMethod(model.getClientAuthMethod())
        .isSslEnabled(model.getIsSslEnabled())
        .userIdentifier(model.getUserIdentifier())
        .audienceClaims(model.getAudienceClaims())
        .build();
  }
}
