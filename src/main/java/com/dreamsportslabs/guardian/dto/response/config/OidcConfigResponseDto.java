package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcIdTokenSigningAlgValue;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.dreamsportslabs.guardian.constant.OidcSubjectType;
import com.dreamsportslabs.guardian.constant.OidcTokenEndpointAuthMethod;
import com.dreamsportslabs.guardian.dao.model.config.OidcConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OidcConfigResponseDto {
  private String tenantId;
  private String issuer;
  private String authorizationEndpoint;
  private String tokenEndpoint;
  private String userinfoEndpoint;
  private String revocationEndpoint;
  private String jwksUri;
  private List<OidcGrantType> grantTypesSupported;
  private List<OidcResponseType> responseTypesSupported;
  private List<OidcSubjectType> subjectTypesSupported;
  private List<OidcIdTokenSigningAlgValue> idTokenSigningAlgValuesSupported;
  private List<OidcTokenEndpointAuthMethod> tokenEndpointAuthMethodsSupported;
  private String loginPageUri;
  private String consentPageUri;
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
