package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequiredString;

import lombok.Data;

@Data
public class CreateTenantRequestDto {
  private String id;
  private String name;

  public void validate() {
    validateRequiredString(id, "id", 10);
    validateRequiredString(name, "name", 256);
  }
}
