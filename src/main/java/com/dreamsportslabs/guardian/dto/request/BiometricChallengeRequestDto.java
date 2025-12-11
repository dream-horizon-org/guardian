package com.dreamsportslabs.guardian.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BiometricChallengeRequestDto {
  @NotBlank(message = "refresh_token is required")
  private String refreshToken;

  @NotNull(message = "client_id is required")
  private String clientId;

  @NotNull(message = "device_metadata is required")
  @Valid
  private DeviceMetadataDto deviceMetadata;
}
