package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.Utils.requireNonBlankIfPresent;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateFbConfigRequestDto {
  @JsonProperty("app_id")
  @Size(max = 256, message = "app_id cannot exceed 256 characters")
  private String appId;

  @JsonProperty("app_secret")
  @Size(max = 256, message = "app_secret cannot exceed 256 characters")
  private String appSecret;

  @JsonProperty("send_app_secret")
  private Boolean sendAppSecret;

  public void validate() {
    requireAtLeastOneField(appId, appSecret, sendAppSecret);

    requireNonBlankIfPresent(appId, "app_id");
    requireNonBlankIfPresent(appSecret, "app_secret");
  }
}
