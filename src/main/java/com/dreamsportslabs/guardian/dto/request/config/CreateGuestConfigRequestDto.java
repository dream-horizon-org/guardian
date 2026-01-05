package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequiredString;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class CreateGuestConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("is_encrypted")
  private Boolean isEncrypted;

  @JsonProperty("secret_key")
  private String secretKey;

  @JsonProperty("allowed_scopes")
  private List<String> allowedScopes;

  public void validate() {
    validateRequiredString(tenantId, "tenant_id", 10);
    validateString(secretKey, "secret_key", 16, false);
  }
}
