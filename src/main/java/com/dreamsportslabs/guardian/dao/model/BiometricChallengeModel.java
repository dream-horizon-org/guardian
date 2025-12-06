package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.dto.request.DeviceMetadataDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class BiometricChallengeModel {
  @JsonProperty("state")
  private String state;

  @JsonProperty("challenge")
  private String challenge;

  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("device_metadata")
  private DeviceMetadataDto deviceMetadata;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("expiry")
  private Long expiry;
}
