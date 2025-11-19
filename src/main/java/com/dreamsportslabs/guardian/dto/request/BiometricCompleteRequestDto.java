package com.dreamsportslabs.guardian.dto.request;

import com.dreamsportslabs.guardian.validation.annotation.ValidPublicKey;
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
public class BiometricCompleteRequestDto {
  @NotBlank(message = "refresh_token is required")
  @JsonProperty("refresh_token")
  private String refreshToken;

  @NotBlank(message = "state is required")
  @JsonProperty("state")
  private String state;

  @NotBlank(message = "client_id is required")
  @JsonProperty("client_id")
  private String clientId;

  @NotBlank(message = "credential_id is required")
  @JsonProperty("credential_id")
  private String credentialId;

  @ValidPublicKey
  @JsonProperty("public_key")
  private String publicKey;

  @NotBlank(message = "signature is required")
  @JsonProperty("signature")
  private String signature;

  @NotNull(message = "device_metadata is required")
  @Valid
  @JsonProperty("device_metadata")
  private DeviceMetadataDto deviceMetadata;
}
