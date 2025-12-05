package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateTenantRequestDto {
  @JsonProperty("id")
  private String tenantId;

  private String name;

  public void validate() {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("id is required");
    }

    if (StringUtils.isBlank(name)) {
      throw INVALID_REQUEST.getCustomException("name is required");
    }

    if (name.length() > 256) {
      throw INVALID_REQUEST.getCustomException("name cannot exceed 256 characters");
    }

    if (name.trim().length() == 0) {
      throw INVALID_REQUEST.getCustomException("name cannot be empty or whitespace only");
    }
  }
}
