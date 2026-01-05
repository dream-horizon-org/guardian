package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import lombok.Data;

@Data
public class UpdateAdminConfigRequestDto {
  private String username;
  private String password;

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateAdminConfigRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(req.getUsername(), req.getPassword());

    validateString(req.getUsername(), "username", 50, true);
    validateString(req.getPassword(), "password", 50, true);
  }
}
