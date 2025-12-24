package com.dreamsportslabs.guardian.dto.request;

import com.dreamsportslabs.guardian.validation.annotation.ValidPublicKey;
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
public class BiometricCompleteRequestDto {
  @NotBlank(message = "refresh_token is required")
  private String refreshToken;

  @NotBlank(message = "state is required")
  private String state;

  @NotBlank(message = "client_id is required")
  private String clientId;

  @NotBlank(message = "credential_id is required")
  private String credentialId;

  @ValidPublicKey private String publicKey;

  @NotBlank(message = "signature is required")
  private String signature;

  @NotNull(message = "device_metadata is required")
  @Valid
  private DeviceMetadataDto deviceMetadata;
}
