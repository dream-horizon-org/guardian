package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequiredString;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateAdminConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  private String username;
  private String password;

  public void validate() {
    validateRequiredString(tenantId, "tenant_id", 10);
    validateRequiredString(username, "username", 50);
    validateRequiredString(password, "password", 50);
  }
}
