package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateUserConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("host")
  private String host;

  @JsonProperty("port")
  private Integer port;

  @JsonProperty("get_user_path")
  private String getUserPath;

  @JsonProperty("create_user_path")
  private String createUserPath;

  @JsonProperty("authenticate_user_path")
  private String authenticateUserPath;

  @JsonProperty("add_provider_path")
  private String addProviderPath;

  @JsonProperty("send_provider_details")
  private Boolean sendProviderDetails;

  public void validate() {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant_id is required");
    }

    if (StringUtils.isBlank(host)) {
      throw INVALID_REQUEST.getCustomException("host is required");
    }

    if (host.length() > 256) {
      throw INVALID_REQUEST.getCustomException("host cannot exceed 256 characters");
    }

    if (port != null && (port < 1 || port > 65535)) {
      throw INVALID_REQUEST.getCustomException("port must be between 1 and 65535");
    }

    if (StringUtils.isBlank(getUserPath)) {
      throw INVALID_REQUEST.getCustomException("get_user_path is required");
    }

    if (getUserPath.length() > 256) {
      throw INVALID_REQUEST.getCustomException("get_user_path cannot exceed 256 characters");
    }

    if (StringUtils.isBlank(createUserPath)) {
      throw INVALID_REQUEST.getCustomException("create_user_path is required");
    }

    if (createUserPath.length() > 256) {
      throw INVALID_REQUEST.getCustomException("create_user_path cannot exceed 256 characters");
    }

    if (StringUtils.isBlank(authenticateUserPath)) {
      throw INVALID_REQUEST.getCustomException("authenticate_user_path is required");
    }

    if (authenticateUserPath.length() > 256) {
      throw INVALID_REQUEST.getCustomException(
          "authenticate_user_path cannot exceed 256 characters");
    }

    if (StringUtils.isBlank(addProviderPath)) {
      throw INVALID_REQUEST.getCustomException("add_provider_path is required");
    }

    if (addProviderPath.length() > 256) {
      throw INVALID_REQUEST.getCustomException("add_provider_path cannot exceed 256 characters");
    }
  }
}
