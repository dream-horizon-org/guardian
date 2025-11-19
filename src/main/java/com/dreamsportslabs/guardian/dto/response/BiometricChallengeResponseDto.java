package com.dreamsportslabs.guardian.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BiometricChallengeResponseDto {
  private String state;

  private String challenge;

  private Integer expiresIn;

  private String credentialId;
}
