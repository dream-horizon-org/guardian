package com.dreamsportslabs.guardian.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BiometricChallengeRequestDto {
  @NotBlank(message = "refresh_token is required")
  @JsonProperty("refresh_token")
  private String refreshToken;

  @NotNull(message = "client_id is required")
  @JsonProperty("client_id")
  private String clientId;

  @NotNull(message = "device_metadata is required")
  @Valid
  @JsonProperty("device_metadata")
  private DeviceMetadataDto deviceMetadata;
}
