package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateFbConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("app_id")
  private String appId;

  @JsonProperty("app_secret")
  private String appSecret;

  @JsonProperty("send_app_secret")
  private Boolean sendAppSecret;

  public void validate() {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot be blank");
    }
    if (tenantId.length() > 10) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot exceed 10 characters");
    }

    if (StringUtils.isBlank(appId)) {
      throw INVALID_REQUEST.getCustomException("app_id cannot be blank");
    }
    if (appId.length() > 256) {
      throw INVALID_REQUEST.getCustomException("app_id cannot exceed 256 characters");
    }

    if (StringUtils.isBlank(appSecret)) {
      throw INVALID_REQUEST.getCustomException("app_secret cannot be blank");
    }
    if (appSecret.length() > 256) {
      throw INVALID_REQUEST.getCustomException("app_secret cannot exceed 256 characters");
    }
  }
}
