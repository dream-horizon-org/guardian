package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateInteger;

import lombok.Data;

@Data
public class UpdateAuthCodeConfigRequestDto {
  private Integer ttl;
  private Integer length;

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateAuthCodeConfigRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(req.getTtl(), req.getLength());

    validateInteger(req.getTtl(), "ttl", 1);
    validateInteger(req.getLength(), "length", 1);
  }
}
