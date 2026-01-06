package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateFbConfigRequestDto {
  @JsonProperty("app_id")
  private String appId;

  @JsonProperty("app_secret")
  private String appSecret;

  @JsonProperty("send_app_secret")
  private Boolean sendAppSecret;

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateFbConfigRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(req.getAppId(), req.getAppSecret(), req.getSendAppSecret());

    validateString(req.getAppId(), "app_id", 256, true);
    validateString(req.getAppSecret(), "app_secret", 256, true);
  }
}
