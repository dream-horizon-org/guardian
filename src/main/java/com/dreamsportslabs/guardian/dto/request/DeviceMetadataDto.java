package com.dreamsportslabs.guardian.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeviceMetadataDto {
  @NotBlank(message = "platform is required")
  @Pattern(regexp = "^(?i)(ios|android)$", message = "Invalid platform. Must be 'ios' or 'android'")
  private String platform;

  @NotBlank(message = "device_id is required")
  private String deviceId;

  private String deviceModel;

  private String osVersion;

  private String appVersion;

  private String deviceName;
}
