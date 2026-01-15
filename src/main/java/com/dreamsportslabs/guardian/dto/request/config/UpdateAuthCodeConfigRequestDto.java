package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateInteger;

import lombok.Getter;

@Getter
public class UpdateAuthCodeConfigRequestDto {
  private Integer ttl;
  private Integer length;

  public void validate() {
    requireAtLeastOneField(ttl, length);
    validateInteger(ttl, "ttl", 1, false);
    validateInteger(length, "length", 1, false);
  }
}
