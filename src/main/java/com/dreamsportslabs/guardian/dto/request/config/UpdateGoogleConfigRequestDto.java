package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.dreamsportslabs.guardian.validation.annotation.NotBlankIfPresent;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateGoogleConfigRequestDto {
  @NotBlankIfPresent(message = "client_id cannot be blank")
  @Size(max = 256, message = "client_id cannot exceed 256 characters")
  private String clientId;

  @NotBlankIfPresent(message = "client_secret cannot be blank")
  @Size(max = 256, message = "client_secret cannot exceed 256 characters")
  private String clientSecret;

  public void validate() {
    requireAtLeastOneField(clientId, clientSecret);
  }
}
