package com.dreamsportslabs.guardian.dto.request.config;

import com.dreamsportslabs.guardian.constant.OidcGrantType;
import com.dreamsportslabs.guardian.constant.OidcIdTokenSigningAlgValue;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import com.dreamsportslabs.guardian.constant.OidcSubjectType;
import com.dreamsportslabs.guardian.constant.OidcTokenEndpointAuthMethod;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateOidcConfigRequestDto {
  @NotNull(message = "issuer cannot be null")
  @Size(max = 255, message = "issuer cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "issuer must be a valid HTTP/HTTPS URL")
  private String issuer;

  @NotNull(message = "authorization_endpoint cannot be null")
  @Size(max = 255, message = "authorization_endpoint cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "authorization_endpoint must be a valid HTTP/HTTPS URL")
  private String authorizationEndpoint;

  @NotNull(message = "token_endpoint cannot be null")
  @Size(max = 255, message = "token_endpoint cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "token_endpoint must be a valid HTTP/HTTPS URL")
  private String tokenEndpoint;

  @NotNull(message = "userinfo_endpoint cannot be null")
  @Size(max = 255, message = "userinfo_endpoint cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "userinfo_endpoint must be a valid HTTP/HTTPS URL")
  private String userinfoEndpoint;

  @NotNull(message = "revocation_endpoint cannot be null")
  @Size(max = 255, message = "revocation_endpoint cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "revocation_endpoint must be a valid HTTP/HTTPS URL")
  private String revocationEndpoint;

  @NotNull(message = "jwks_uri cannot be null")
  @Size(max = 255, message = "jwks_uri cannot exceed 255 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "jwks_uri must be a valid HTTP/HTTPS URL")
  private String jwksUri;

  @NotNull private List<OidcGrantType> grantTypesSupported = new ArrayList<>();

  @NotNull private List<OidcResponseType> responseTypesSupported = new ArrayList<>();

  @NotNull private List<OidcSubjectType> subjectTypesSupported = new ArrayList<>();

  @NotNull
  private List<OidcIdTokenSigningAlgValue> idTokenSigningAlgValuesSupported = new ArrayList<>();

  @NotNull
  private List<OidcTokenEndpointAuthMethod> tokenEndpointAuthMethodsSupported = new ArrayList<>();

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
}
