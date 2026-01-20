package com.dreamsportslabs.guardian.dto.request.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

@Getter
public class CreateGuestConfigRequestDto {
  @JsonProperty("is_encrypted")
  private Boolean isEncrypted;

  @JsonProperty("secret_key")
  @Size(max = 16, message = "secret_key cannot exceed 16 characters")
  private String secretKey;

  @JsonProperty("allowed_scopes")
  private List<String> allowedScopes;
}
