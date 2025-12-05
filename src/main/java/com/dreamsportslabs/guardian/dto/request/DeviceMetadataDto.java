package com.dreamsportslabs.guardian.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeviceMetadataDto {
  @Pattern(
      regexp = "^$|^(?i)(ios|android)$",
      message = "Invalid platform. Must be 'ios' or 'android'")
  @JsonProperty("platform")
  private String platform;

  @NotBlank(message = "device_id is required")
  @JsonProperty("device_id")
  private String deviceId;

  @JsonProperty("device_model")
  private String deviceModel;

  @JsonProperty("os_version")
  private String osVersion;

  @JsonProperty("app_version")
  private String appVersion;

  @JsonProperty("device_name")
  private String deviceName;
}
