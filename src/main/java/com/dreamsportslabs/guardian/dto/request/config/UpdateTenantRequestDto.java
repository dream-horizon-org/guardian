package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import lombok.Data;

@Data
public class UpdateTenantRequestDto {
  private String name;

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateTenantRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(req.getName());

    validateString(req.getName(), "name", 256, true);
  }
}
