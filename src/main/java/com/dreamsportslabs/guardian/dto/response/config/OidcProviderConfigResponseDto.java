package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.constant.ClientAuthMethod;
import com.dreamsportslabs.guardian.dao.model.config.OidcProviderConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OidcProviderConfigResponseDto {
  private String tenantId;
  private String providerName;
  private String issuer;
  private String jwksUrl;
  private String tokenUrl;
  private String clientId;
  private String clientSecret;
  private String redirectUri;
  private ClientAuthMethod clientAuthMethod;
  private Boolean isSslEnabled;
  private String userIdentifier;
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
