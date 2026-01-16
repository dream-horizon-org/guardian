package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateInteger;

import lombok.Getter;

@Getter
public class CreateAuthCodeConfigRequestDto {
  private Integer ttl;
  private Integer length;

  public void validate() {
    validateInteger(ttl, "ttl", 1, true);
    validateInteger(length, "length", 1, true);
  }
}
