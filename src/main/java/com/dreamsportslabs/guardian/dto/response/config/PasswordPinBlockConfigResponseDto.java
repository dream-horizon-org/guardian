package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.PasswordPinBlockConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PasswordPinBlockConfigResponseDto {
  private String tenantId;
  private Integer attemptsAllowed;
  private Integer attemptsWindowSeconds;
  private Integer blockIntervalSeconds;

  public static PasswordPinBlockConfigResponseDto from(
      String tenantId, PasswordPinBlockConfigModel model) {
    return PasswordPinBlockConfigResponseDto.builder()
        .tenantId(tenantId)
        .attemptsAllowed(model.getAttemptsAllowed())
        .attemptsWindowSeconds(model.getAttemptsWindowSeconds())
        .blockIntervalSeconds(model.getBlockIntervalSeconds())
        .build();
  }
}
