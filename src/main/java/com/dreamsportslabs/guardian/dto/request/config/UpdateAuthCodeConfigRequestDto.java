package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import lombok.Data;

@Data
public class UpdateAuthCodeConfigRequestDto {
  private Integer ttl;

  private Integer length;

  public void validate() {
    boolean hasFields = false;

    if (ttl != null) {
      hasFields = true;
      if (ttl < 1) {
        throw INVALID_REQUEST.getCustomException("ttl must be greater than 0");
      }
    }

    if (length != null) {
      hasFields = true;
      if (length < 1) {
        throw INVALID_REQUEST.getCustomException("length must be greater than 0");
      }
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
