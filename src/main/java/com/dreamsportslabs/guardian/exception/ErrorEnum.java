package com.dreamsportslabs.guardian.exception;

import static com.dreamsportslabs.guardian.constant.Constants.UNAUTHORIZED_ERROR_CODE;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import lombok.Getter;

public enum ErrorEnum {
  INVALID_REQUEST("invalid_request", "Invalid request params", 400),
  UNAUTHORIZED(UNAUTHORIZED_ERROR_CODE, "Unauthorized", 401),
  INTERNAL_SERVER_ERROR("internal_server_error", "Something went wrong", 500),
  USER_SERVICE_ERROR_400("user_service_error", "User service error", 400),
  USER_SERVICE_ERROR("user_service_error", "User service error", 500),
  SMS_SERVICE_ERROR("sms_service_error", "SMS service error", 500),
  EMAIL_SERVICE_ERROR("email_service_error", "Email service error", 500),
  INVALID_QUERY_PARAM("invalid_query_param", "Invalid query param", 400),
  DECRYPTION_FAILED("invalid_guest_identifier", "Invalid guest identifier", 400),
  INVALID_SCOPE("invalid_scope", "Invalid scopes", 400),

  INVALID_CODE("invalid_code", "Invalid code", 400),
  NO_FIELDS_TO_UPDATE("no_fields_to_update", "No fields for update", 400),

  INVALID_STATE("invalid_state", "Invalid state", 400),
  RESENDS_EXHAUSTED("resends_exhausted", "Resends exhausted", 400),
  RESEND_NOT_ALLOWED("resends_not_allowed", "Resend triggered too quick, Try again later", 400),
  INCORRECT_OTP("incorrect_otp", "Incorrect otp", 400),
  RETRIES_EXHAUSTED("retries_exhausted", "Retries exhausted", 400),

  USER_EXISTS("user_exists", "User already exists", 400),
  USER_NOT_EXISTS("user_not_exists", "User does not exist", 400),

  INVALID_IDP_TOKEN("invalid_idp_token", "Invalid identity provider token", 400),
  INVALID_IDP_CODE("invalid_idp_code", "Missing ID token in provider response", 400),
  PROVIDER_TOKENS_EXCHANGE_FAILED(
      "token_exchange_failed",
      "Error occurred while exchanging authorization_code for provider tokens",
      400),
  INVALID_USER_IDENTIFIER(
      "invalid_user_identifier",
      "No valid user identifier found from the identifier provided",
      400),
  INVALID_IDENTIFIER_TYPE("invalid_identifier_type", "Invalid identifier type", 400),
  FLOW_BLOCKED("flow_blocked", "API is blocked for this userIdentifier", 403),
  SCOPE_ALREADY_EXISTS("scope_already_exists", "scope already exists", 400),
  INVALID_CLIENT("invalid_client", "Client authentication failed", 401),
  CLIENT_NOT_FOUND("client_not_found", "Client not found", 404),
  CLIENT_ALREADY_EXISTS("client_already_exists", "Client already exists", 400),
  UNPROCESSABLE_ENTITIES("unprocessable_entities", "Unprocessable entities", 422),

  OIDC_CONFIG_NOT_EXISTS(
      "oidc_config_not_exists", "OIDC config does not exist for this tenant", 400),
  SCOPE_NOT_FOUND("scope_not_found", "Scope not found", 400),
  FEATURE_NOT_CONFIGURED(
      "feature_not_configured", "This feature is not configured for the tenant", 400),
  GOOGLE_AUTH_NOT_CONFIGURED(
      "google_auth_not_configured", "Google authentication is not configured for this tenant", 400),
  FACEBOOK_AUTH_NOT_CONFIGURED(
      "facebook_auth_not_configured",
      "Facebook authentication is not configured for this tenant",
      400),
  OIDC_PROVIDER_NOT_CONFIGURED(
      "oidc_provider_not_configured", "OIDC provider is not configured for this tenant", 400),
  OTP_NOT_CONFIGURED("otp_not_configured", "OTP feature is not configured for this tenant", 400),
  EMAIL_NOT_CONFIGURED(
      "email_not_configured", "Email service is not configured for this tenant", 400),
  SMS_NOT_CONFIGURED("sms_not_configured", "SMS service is not configured for this tenant", 400),
  CONTACT_VERIFY_NOT_CONFIGURED(
      "contact_verify_not_configured",
      "Contact verification is not configured for this tenant",
      400),
  GUEST_LOGIN_NOT_CONFIGURED(
      "guest_login_not_configured", "Guest login is not configured for this tenant", 400),
  AUTH_CODE_NOT_CONFIGURED(
      "auth_code_not_configured",
      "Authorization code feature is not configured for this tenant",
      400),
  CHANGELOG_NOT_FOUND("changelog_not_found", "Changelog not found", 400),
  TENANT_NOT_FOUND("tenant_not_found", "Tenant not found", 400),
  TENANT_ALREADY_EXISTS("tenant_already_exists", "Tenant already exists", 400),
  TENANT_NAME_ALREADY_EXISTS("tenant_name_already_exists", "Tenant name already exists", 400),
  USER_CONFIG_NOT_FOUND("user_config_not_found", "User config not found", 404),
  TOKEN_CONFIG_NOT_FOUND("token_config_not_found", "Token config not found", 404),
  EMAIL_CONFIG_NOT_FOUND("email_config_not_found", "Email config not found", 404),
  EMAIL_CONFIG_ALREADY_EXISTS("email_config_already_exists", "Email config already exists", 400),
  AUTH_CODE_CONFIG_NOT_FOUND("auth_code_config_not_found", "Auth code config not found", 404),
  AUTH_CODE_CONFIG_ALREADY_EXISTS(
      "auth_code_config_already_exists", "Auth code config already exists", 400);
  OTP_CONFIG_NOT_FOUND("otp_config_not_found", "OTP config not found", 404),
  OTP_CONFIG_ALREADY_EXISTS("otp_config_already_exists", "OTP config already exists", 400),
  CONTACT_VERIFY_CONFIG_NOT_FOUND(
      "contact_verify_config_not_found", "Contact verify config not found", 404),
  CONTACT_VERIFY_CONFIG_ALREADY_EXISTS(
      "contact_verify_config_already_exists", "Contact verify config already exists", 400);
  OIDC_PROVIDER_CONFIG_NOT_FOUND(
      "oidc_provider_config_not_found", "OIDC provider config not found", 404),
  OIDC_PROVIDER_CONFIG_ALREADY_EXISTS(
      "oidc_provider_config_already_exists", "OIDC provider config already exists", 400);
  ADMIN_CONFIG_NOT_FOUND("admin_config_not_found", "Admin config not found", 404),
  ADMIN_CONFIG_ALREADY_EXISTS("admin_config_already_exists", "Admin config already exists", 400);
  OIDC_CONFIG_NOT_FOUND("oidc_config_not_found", "OIDC config not found", 404),
  OIDC_CONFIG_ALREADY_EXISTS("oidc_config_already_exists", "OIDC config already exists", 400),
  GUEST_CONFIG_NOT_FOUND("guest_config_not_found", "Guest config not found", 404),
  GUEST_CONFIG_ALREADY_EXISTS("guest_config_already_exists", "Guest config already exists", 400);

  private final String code;
  private final String message;
  private final int httpStatusCode;

  @Getter private final WebApplicationException exception;

  ErrorEnum(String code, String message, int httpStatusCode) {
    this.code = code;
    this.message = message;
    this.httpStatusCode = httpStatusCode;
    Response response =
        Response.status(httpStatusCode)
            .header("Content-Type", "application/json")
            .entity(new ErrorEntity(code, message))
            .build();
    this.exception = new WebApplicationException(response);
  }

  public WebApplicationException getException(Throwable t) {
    String message = t.getMessage() != null ? t.getMessage() : this.message;

    Response response =
        Response.status(this.httpStatusCode)
            .header("Content-Type", "application/json")
            .entity(new ErrorEntity(this.code, message))
            .build();
    return new WebApplicationException(t, response);
  }

  public WebApplicationException getCustomException(String message) {
    message = message == null ? this.message : message;

    Response response =
        Response.status(this.httpStatusCode)
            .header("Content-Type", "application/json")
            .entity(new ErrorEntity(this.code, message))
            .build();
    return new WebApplicationException(response);
  }

  public WebApplicationException getCustomException(Map<String, Object> data) {
    Response response =
        Response.status(this.httpStatusCode)
            .header("Content-Type", "application/json")
            .entity(new ErrorEntity(this.code, this.message, data))
            .build();
    return new WebApplicationException(response);
  }

  public WebApplicationException getCustomException(String message, Map<String, Object> data) {
    message = message == null ? this.message : message;

    Response response =
        Response.status(this.httpStatusCode)
            .header("Content-Type", "application/json")
            .entity(new ErrorEntity(this.code, message, data))
            .build();
    return new WebApplicationException(response);
  }

  public WebApplicationException getCustomException(String code, String message) {
    Response response =
        Response.status(this.httpStatusCode)
            .header("Content-Type", "application/json")
            .entity(new ErrorEntity(code, message))
            .build();
    return new WebApplicationException(response);
  }

  @Getter
  public static class ErrorEntity {
    final Error error;

    ErrorEntity(String code, String message) {
      this.error = new Error(code, message);
    }

    ErrorEntity(String code, String message, Map<String, Object> data) {
      this.error = new Error(code, message, data);
    }

    @Getter
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class Error {
      final String code;
      final String message;
      final Map<String, Object> metadata;
      final Map<String, Object> meta_data;

      Error(String code, String message) {
        this(code, message, null);
      }

      Error(String code, String message, Map<String, Object> metadata) {
        this.code = code;
        this.message = message;
        this.metadata = metadata;
        this.meta_data = metadata;
      }
    }
  }
}
