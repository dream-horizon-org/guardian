package com.dreamsportslabs.guardian.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricChallengeResponseDto {
  @JsonProperty("state")
  private String state;

  @JsonProperty("challenge")
  private String challenge;

  @JsonProperty("expires_in")
  private Integer expiresIn;
}
