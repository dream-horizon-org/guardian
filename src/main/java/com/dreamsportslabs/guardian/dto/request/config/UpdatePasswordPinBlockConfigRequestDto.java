package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdatePasswordPinBlockConfigRequestDto {

  @Min(value = 1, message = "attempts_allowed must be greater than or equal to 1")
  private Integer attemptsAllowed;

  @Min(value = 1, message = "attempts_window_seconds must be greater than or equal to 1")
  private Integer attemptsWindowSeconds;

  @Min(value = 1, message = "block_interval_seconds must be greater than or equal to 1")
  private Integer blockIntervalSeconds;

  public void validate() {
    requireAtLeastOneField(attemptsAllowed, attemptsWindowSeconds, blockIntervalSeconds);
  }
}
