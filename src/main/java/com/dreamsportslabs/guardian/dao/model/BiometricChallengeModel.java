package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.dto.request.DeviceMetadataDto;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BiometricChallengeModel {

  private String state;

  private String challenge;

  private String clientId;

  private String userId;

  private DeviceMetadataDto deviceMetadata;

  private String refreshToken;

  private Long expiry;
}
