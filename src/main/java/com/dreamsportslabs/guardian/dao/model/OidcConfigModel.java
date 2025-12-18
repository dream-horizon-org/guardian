package com.dreamsportslabs.guardian.dao.model;

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
public class OidcConfigModel {
  private String tenantId;
  private String issuer;
  private String authorizationEndpoint;
  private String tokenEndpoint;
  private String userinfoEndpoint;
  private String revocationEndpoint;
  private String jwksUri;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String grantTypesSupported;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String responseTypesSupported;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String subjectTypesSupported;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String idTokenSigningAlgValuesSupported;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String tokenEndpointAuthMethodsSupported;

  private String loginPageUri;
  private String consentPageUri;
  private Integer authorizeTtl;
}
