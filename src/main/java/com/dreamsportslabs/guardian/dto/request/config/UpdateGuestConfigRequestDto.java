package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class UpdateGuestConfigRequestDto {
  @JsonProperty("is_encrypted")
  private Boolean isEncrypted;

  @JsonProperty("secret_key")
  private String secretKey;

  @JsonProperty("allowed_scopes")
  private List<String> allowedScopes;

  public void validate() {
    boolean hasFields = false;

    if (isEncrypted != null) {
      hasFields = true;
    }

    if (secretKey != null) {
      hasFields = true;
      if (secretKey.length() > 16) {
        throw INVALID_REQUEST.getCustomException("secret_key cannot exceed 16 characters");
      }
    }

    if (allowedScopes != null) {
      hasFields = true;
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
