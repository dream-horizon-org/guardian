package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot be blank");
    }
    if (tenantId.length() > 10) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot exceed 10 characters");
    }

    if (secretKey != null && secretKey.length() > 16) {
      throw INVALID_REQUEST.getCustomException("secret_key cannot exceed 16 characters");
    }
  }
}
