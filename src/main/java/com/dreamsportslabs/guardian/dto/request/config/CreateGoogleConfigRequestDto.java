package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequiredString;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateGoogleConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("client_secret")
  private String clientSecret;

  public void validate() {
    validateRequiredString(tenantId, "tenant_id", 10);
    validateRequiredString(clientId, "client_id", 256);
    validateRequiredString(clientSecret, "client_secret", 256);
  }
}
