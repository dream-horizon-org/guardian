package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateTenantRequestDto {
  private String id;
  private String name;

  public void validate() {
    if (StringUtils.isBlank(id)) {
      throw INVALID_REQUEST.getCustomException("id is required");
    }

    if (id.length() > 10) {
      throw INVALID_REQUEST.getCustomException("id cannot exceed 10 characters");
    }

    if (StringUtils.isBlank(name)) {
      throw INVALID_REQUEST.getCustomException("name is required");
    }

    if (name.length() > 256) {
      throw INVALID_REQUEST.getCustomException("name cannot exceed 256 characters");
    }
  }
}
