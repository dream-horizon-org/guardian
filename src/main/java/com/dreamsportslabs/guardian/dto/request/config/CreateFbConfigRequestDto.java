package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_SEND_APP_SECRET;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateFbConfigRequestDto {
  @NotBlank(message = "app_id cannot be blank")
  @Size(max = 256, message = "app_id cannot exceed 256 characters")
  private String appId;

  @NotBlank(message = "app_secret cannot be blank")
  @Size(max = 256, message = "app_secret cannot exceed 256 characters")
  private String appSecret;

  @NotNull private Boolean sendAppSecret = DEFAULT_SEND_APP_SECRET;
}
