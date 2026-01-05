package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UpdateOidcConfigRequestDto {
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

  public void validate() {
    boolean hasFields = false;

    if (issuer != null) {
      hasFields = true;
      if (StringUtils.isBlank(issuer)) {
        throw INVALID_REQUEST.getCustomException("issuer cannot be blank");
      }
      if (issuer.length() > 255) {
        throw INVALID_REQUEST.getCustomException("issuer cannot exceed 255 characters");
      }
    }

    if (authorizationEndpoint != null) {
      hasFields = true;
      if (StringUtils.isBlank(authorizationEndpoint)) {
        throw INVALID_REQUEST.getCustomException("authorization_endpoint cannot be blank");
      }
      if (authorizationEndpoint.length() > 255) {
        throw INVALID_REQUEST.getCustomException(
            "authorization_endpoint cannot exceed 255 characters");
      }
    }

    if (tokenEndpoint != null) {
      hasFields = true;
      if (StringUtils.isBlank(tokenEndpoint)) {
        throw INVALID_REQUEST.getCustomException("token_endpoint cannot be blank");
      }
      if (tokenEndpoint.length() > 255) {
        throw INVALID_REQUEST.getCustomException("token_endpoint cannot exceed 255 characters");
      }
    }

    if (userinfoEndpoint != null) {
      hasFields = true;
      if (StringUtils.isBlank(userinfoEndpoint)) {
        throw INVALID_REQUEST.getCustomException("userinfo_endpoint cannot be blank");
      }
      if (userinfoEndpoint.length() > 255) {
        throw INVALID_REQUEST.getCustomException("userinfo_endpoint cannot exceed 255 characters");
      }
    }

    if (revocationEndpoint != null) {
      hasFields = true;
      if (StringUtils.isBlank(revocationEndpoint)) {
        throw INVALID_REQUEST.getCustomException("revocation_endpoint cannot be blank");
      }
      if (revocationEndpoint.length() > 255) {
        throw INVALID_REQUEST.getCustomException(
            "revocation_endpoint cannot exceed 255 characters");
      }
    }

    if (jwksUri != null) {
      hasFields = true;
      if (StringUtils.isBlank(jwksUri)) {
        throw INVALID_REQUEST.getCustomException("jwks_uri cannot be blank");
      }
      if (jwksUri.length() > 255) {
        throw INVALID_REQUEST.getCustomException("jwks_uri cannot exceed 255 characters");
      }
    }

    if (grantTypesSupported != null) {
      hasFields = true;
    }

    if (responseTypesSupported != null) {
      hasFields = true;
    }

    if (subjectTypesSupported != null) {
      hasFields = true;
    }

    if (idTokenSigningAlgValuesSupported != null) {
      hasFields = true;
    }

    if (tokenEndpointAuthMethodsSupported != null) {
      hasFields = true;
    }

    if (loginPageUri != null) {
      hasFields = true;
      if (loginPageUri.length() > 512) {
        throw INVALID_REQUEST.getCustomException("login_page_uri cannot exceed 512 characters");
      }
    }

    if (consentPageUri != null) {
      hasFields = true;
      if (consentPageUri.length() > 512) {
        throw INVALID_REQUEST.getCustomException("consent_page_uri cannot exceed 512 characters");
      }
    }

    if (authorizeTtl != null) {
      hasFields = true;
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
