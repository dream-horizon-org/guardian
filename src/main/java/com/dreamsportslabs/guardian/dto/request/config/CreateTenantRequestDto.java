package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import lombok.Getter;

@Getter
public class CreateTenantRequestDto {
  private String id;
  private String name;

  public void validate() {
    validateString(id, "id", 10, true);
    validateString(name, "name", 256, true);
  }
}
