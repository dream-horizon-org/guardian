package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequiredString;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
    validateRequiredString(tenantId, "tenant_id", 10);
    validateRequiredString(appId, "app_id", 256);
    validateRequiredString(appSecret, "app_secret", 256);
  }
}
