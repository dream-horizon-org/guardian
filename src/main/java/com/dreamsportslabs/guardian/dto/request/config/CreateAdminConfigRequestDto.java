package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import lombok.Getter;

@Getter
public class CreateAdminConfigRequestDto {
  private String username;
  private String password;

  public void validate() {
    validateString(username, "username", 50, true);
    validateString(password, "password", 50, true);
  }
}
