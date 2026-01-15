package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import lombok.Getter;

@Getter
public class UpdateAdminConfigRequestDto {
  private String username;
  private String password;

  public void validate() {
    requireAtLeastOneField(username, password);
    validateString(username, "username", 50, false);
    validateString(password, "password", 50, false);
  }
}
