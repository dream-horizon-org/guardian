package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequiredInteger;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequiredString;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateAuthCodeConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  private Integer ttl;
  private Integer length;

  public void validate() {
    validateRequiredString(tenantId, "tenant_id", 10);
    validateRequiredInteger(ttl, "ttl", 1);
    validateRequiredInteger(length, "length", 1);
  }
}
