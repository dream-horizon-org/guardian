package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UpdateAdminConfigRequestDto {
  private String username;

  private String password;

  public void validate() {
    boolean hasFields = false;

    if (username != null) {
      hasFields = true;
      if (StringUtils.isBlank(username)) {
        throw INVALID_REQUEST.getCustomException("username cannot be blank");
      }
      if (username.length() > 50) {
        throw INVALID_REQUEST.getCustomException("username cannot exceed 50 characters");
      }
    }

    if (password != null) {
      hasFields = true;
      if (StringUtils.isBlank(password)) {
        throw INVALID_REQUEST.getCustomException("password cannot be blank");
      }
      if (password.length() > 50) {
        throw INVALID_REQUEST.getCustomException("password cannot exceed 50 characters");
      }
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
