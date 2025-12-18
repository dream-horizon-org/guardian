package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UpdateTenantRequestDto {
  private String name;

  public void validate() {

    if (name == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
    if (StringUtils.isBlank(name)) {
      throw INVALID_REQUEST.getCustomException("name cannot be blank");
    }

    if (name.length() > 256) {
      throw INVALID_REQUEST.getCustomException("name cannot exceed 256 characters");
    }
  }
}
