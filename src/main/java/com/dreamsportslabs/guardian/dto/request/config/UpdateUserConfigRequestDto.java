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
    boolean hasFields = false;

    if (isSslEnabled != null) {
      hasFields = true;
    }

    if (StringUtils.isNotBlank(host)) {
      hasFields = true;
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

    if (StringUtils.isNotBlank(getUserPath)) {
      hasFields = true;
      if (getUserPath.length() > 256) {
        throw INVALID_REQUEST.getCustomException("get_user_path cannot exceed 256 characters");
      }
    }

    if (StringUtils.isNotBlank(createUserPath)) {
      hasFields = true;
      if (createUserPath.length() > 256) {
        throw INVALID_REQUEST.getCustomException("create_user_path cannot exceed 256 characters");
      }
    }

    if (StringUtils.isNotBlank(authenticateUserPath)) {
      hasFields = true;
      if (authenticateUserPath.length() > 256) {
        throw INVALID_REQUEST.getCustomException("authenticate_user_path cannot exceed 256 characters");
      }
    }

    if (StringUtils.isNotBlank(addProviderPath)) {
      hasFields = true;
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

