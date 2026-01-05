package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UpdateFbConfigRequestDto {
  @JsonProperty("app_id")
  private String appId;

  @JsonProperty("app_secret")
  private String appSecret;

  @JsonProperty("send_app_secret")
  private Boolean sendAppSecret;

  public void validate() {
    boolean hasFields = false;

    if (appId != null) {
      hasFields = true;
      if (StringUtils.isBlank(appId)) {
        throw INVALID_REQUEST.getCustomException("app_id cannot be blank");
      }
      if (appId.length() > 256) {
        throw INVALID_REQUEST.getCustomException("app_id cannot exceed 256 characters");
      }
    }

    if (appSecret != null) {
      hasFields = true;
      if (StringUtils.isBlank(appSecret)) {
        throw INVALID_REQUEST.getCustomException("app_secret cannot be blank");
      }
      if (appSecret.length() > 256) {
        throw INVALID_REQUEST.getCustomException("app_secret cannot exceed 256 characters");
      }
    }

    if (sendAppSecret != null) {
      hasFields = true;
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
