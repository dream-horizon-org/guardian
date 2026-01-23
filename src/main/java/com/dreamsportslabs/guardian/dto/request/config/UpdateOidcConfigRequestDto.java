package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcIdTokenSigningAlgValue;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.dreamsportslabs.guardian.constant.OidcSubjectType;
import com.dreamsportslabs.guardian.constant.OidcTokenEndpointAuthMethod;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateOidcConfigRequestDto {
  @Size(max = 255, message = "issuer cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "issuer must be a valid HTTP/HTTPS URL")
  private String issuer;

  @Size(max = 255, message = "authorization_endpoint cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "authorization_endpoint must be a valid HTTP/HTTPS URL")
  private String authorizationEndpoint;

  @Size(max = 255, message = "token_endpoint cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "token_endpoint must be a valid HTTP/HTTPS URL")
  private String tokenEndpoint;

  @Size(max = 255, message = "userinfo_endpoint cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "userinfo_endpoint must be a valid HTTP/HTTPS URL")
  private String userinfoEndpoint;

  @Size(max = 255, message = "revocation_endpoint cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "revocation_endpoint must be a valid HTTP/HTTPS URL")
  private String revocationEndpoint;

  @Size(max = 255, message = "jwks_uri cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "jwks_uri must be a valid HTTP/HTTPS URL")
  private String jwksUri;

  private List<OidcGrantType> grantTypesSupported;

  private List<OidcResponseType> responseTypesSupported;

  private List<OidcSubjectType> subjectTypesSupported;

  private List<OidcIdTokenSigningAlgValue> idTokenSigningAlgValuesSupported;

  private List<OidcTokenEndpointAuthMethod> tokenEndpointAuthMethodsSupported;

  @Size(max = 512, message = "login_page_uri cannot exceed 512 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "login_page_uri must be a valid HTTP/HTTPS URL")
  private String loginPageUri;

  @Size(max = 512, message = "consent_page_uri cannot exceed 512 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "consent_page_uri must be a valid HTTP/HTTPS URL")
  private String consentPageUri;

  private Integer authorizeTtl;

  public void validate() {
    requireAtLeastOneField(
        issuer,
        authorizationEndpoint,
        tokenEndpoint,
        userinfoEndpoint,
        revocationEndpoint,
        jwksUri,
        grantTypesSupported,
        responseTypesSupported,
        subjectTypesSupported,
        idTokenSigningAlgValuesSupported,
        tokenEndpointAuthMethodsSupported,
        loginPageUri,
        consentPageUri,
        authorizeTtl);
  }
}
