package com.dreamsportslabs.guardian.dao.model.config;

import com.dreamsportslabs.guardian.constant.ClientAuthMethod;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class OidcProviderConfigModel {
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
}
