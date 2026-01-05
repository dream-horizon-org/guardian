package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateGoogleConfigRequestDto {
  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("client_secret")
  private String clientSecret;

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateGoogleConfigRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(req.getClientId(), req.getClientSecret());

    validateString(req.getClientId(), "client_id", 256, true);
    validateString(req.getClientSecret(), "client_secret", 256, true);
  }
}
