package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UpdateUserConfigRequestDto {
  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  private String host;

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
    boolean hasFields = false;

    if (isSslEnabled != null) {
      hasFields = true;
    }

    if (host != null) {
      hasFields = true;
      if (StringUtils.isBlank(host)) {
        throw INVALID_REQUEST.getCustomException("host cannot be blank");
      }
      if (host.length() > 256) {
        throw INVALID_REQUEST.getCustomException("host cannot exceed 256 characters");
      }
    }

    if (port != null) {
      hasFields = true;
      if (port < 1 || port > 65535) {
        throw INVALID_REQUEST.getCustomException("port must be between 1 and 65535");
      }
    }

    if (getUserPath != null) {
      hasFields = true;
      if (StringUtils.isBlank(getUserPath)) {
        throw INVALID_REQUEST.getCustomException("get_user_path cannot be blank");
      }
      if (getUserPath.length() > 256) {
        throw INVALID_REQUEST.getCustomException("get_user_path cannot exceed 256 characters");
      }
    }

    if (createUserPath != null) {
      hasFields = true;
      if (StringUtils.isBlank(createUserPath)) {
        throw INVALID_REQUEST.getCustomException("create_user_path cannot be blank");
      }
      if (createUserPath.length() > 256) {
        throw INVALID_REQUEST.getCustomException("create_user_path cannot exceed 256 characters");
      }
    }

    if (authenticateUserPath != null) {
      hasFields = true;
      if (StringUtils.isBlank(authenticateUserPath)) {
        throw INVALID_REQUEST.getCustomException("authenticate_user_path cannot be blank");
      }
      if (authenticateUserPath.length() > 256) {
        throw INVALID_REQUEST.getCustomException(
            "authenticate_user_path cannot exceed 256 characters");
      }
    }

    if (addProviderPath != null) {
      hasFields = true;
      if (StringUtils.isBlank(addProviderPath)) {
        throw INVALID_REQUEST.getCustomException("add_provider_path cannot be blank");
      }
      if (addProviderPath.length() > 256) {
        throw INVALID_REQUEST.getCustomException("add_provider_path cannot exceed 256 characters");
      }
    }

    if (sendProviderDetails != null) {
      hasFields = true;
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
