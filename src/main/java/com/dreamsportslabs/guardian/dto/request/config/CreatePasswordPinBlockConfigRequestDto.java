package com.dreamsportslabs.guardian.dto.request.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreatePasswordPinBlockConfigRequestDto {

  @NotNull
  @Min(value = 1, message = "attempts_allowed must be greater than or equal to 1")
  private Integer attemptsAllowed;

  @NotNull
  @Min(value = 1, message = "attempts_window_seconds must be greater than or equal to 1")
  private Integer attemptsWindowSeconds;

  @NotNull
  @Min(value = 1, message = "block_interval_seconds must be greater than or equal to 1")
  private Integer blockIntervalSeconds;
}
