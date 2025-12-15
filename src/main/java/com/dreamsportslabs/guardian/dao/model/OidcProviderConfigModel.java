package com.dreamsportslabs.guardian.dao.model;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_IDENTIFIER;
import static com.dreamsportslabs.guardian.utils.Utils.JsonToStringDeserializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Setter
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OidcProviderConfigModel {
  private String tenantId;

  private String providerName;

  private String issuer;

  private String jwksUrl;

  private String tokenUrl;

  private String clientId;

  private String clientSecret;

  private String redirectUri;

  private String clientAuthMethod;

  @Builder.Default private Boolean isSslEnabled = DEFAULT_IS_SSL_ENABLED;

  @Builder.Default private String userIdentifier = DEFAULT_USER_IDENTIFIER;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String audienceClaims;
}
