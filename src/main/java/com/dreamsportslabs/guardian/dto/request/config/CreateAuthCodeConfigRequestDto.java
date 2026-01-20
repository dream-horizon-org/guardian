package com.dreamsportslabs.guardian.dto.request.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateAuthCodeConfigRequestDto {
  @NotNull(message = "ttl cannot be null")
  @Min(value = 1, message = "ttl must be greater than or equal to 1")
  private Integer ttl;

  @NotNull(message = "length cannot be null")
  @Min(value = 1, message = "length must be greater than or equal to 1")
  private Integer length;
}
