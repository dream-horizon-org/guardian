package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateAdminConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  private String username;

  private String password;

  public void validate() {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot be blank");
    }
    if (tenantId.length() > 10) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot exceed 10 characters");
    }

    if (StringUtils.isBlank(username)) {
      throw INVALID_REQUEST.getCustomException("username cannot be blank");
    }
    if (username.length() > 50) {
      throw INVALID_REQUEST.getCustomException("username cannot exceed 50 characters");
    }

    if (StringUtils.isBlank(password)) {
      throw INVALID_REQUEST.getCustomException("password cannot be blank");
    }
    if (password.length() > 50) {
      throw INVALID_REQUEST.getCustomException("password cannot exceed 50 characters");
    }
  }
}
