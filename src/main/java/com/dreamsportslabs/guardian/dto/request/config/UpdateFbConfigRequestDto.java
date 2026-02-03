package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.dreamsportslabs.guardian.validation.annotation.NotBlankIfPresent;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateFbConfigRequestDto {
  @NotBlankIfPresent(message = "app_id cannot be blank")
  @Size(max = 256, message = "app_id cannot exceed 256 characters")
  private String appId;

  @NotBlankIfPresent(message = "app_secret cannot be blank")
  @Size(max = 256, message = "app_secret cannot exceed 256 characters")
  private String appSecret;

  private Boolean sendAppSecret;

  public void validate() {
    requireAtLeastOneField(appId, appSecret, sendAppSecret);
  }
}
