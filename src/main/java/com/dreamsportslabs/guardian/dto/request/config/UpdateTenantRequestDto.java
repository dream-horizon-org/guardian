package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import lombok.Getter;

@Getter
public class UpdateTenantRequestDto {
  private String name;

  public void validate() {
    requireAtLeastOneField(name);
    validateString(name, "name", 256, true);
  }
}
