package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateGuestConfigRequestDto {
  private Boolean isEncrypted;

  @Size(max = 16, message = "secret_key cannot exceed 16 characters")
  private String secretKey;

  private List<String> allowedScopes;

  public void validate() {
    requireAtLeastOneField(isEncrypted, secretKey, allowedScopes);
  }
}
