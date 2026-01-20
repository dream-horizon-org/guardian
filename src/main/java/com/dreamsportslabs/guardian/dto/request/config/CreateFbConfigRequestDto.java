package com.dreamsportslabs.guardian.dto.request.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateFbConfigRequestDto {
  @JsonProperty("app_id")
  @NotBlank(message = "app_id cannot be blank")
  @Size(max = 256, message = "app_id cannot exceed 256 characters")
  private String appId;

  @JsonProperty("app_secret")
  @NotBlank(message = "app_secret cannot be blank")
  @Size(max = 256, message = "app_secret cannot exceed 256 characters")
  private String appSecret;

  @JsonProperty("send_app_secret")
  private Boolean sendAppSecret;
}
