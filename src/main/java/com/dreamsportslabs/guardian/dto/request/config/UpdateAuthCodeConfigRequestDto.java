package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class UpdateAuthCodeConfigRequestDto {
  @Min(value = 1, message = "ttl must be greater than or equal to 1")
  private Integer ttl;

  @Min(value = 1, message = "length must be greater than or equal to 1")
  private Integer length;

  public void validate() {
    requireAtLeastOneField(ttl, length);
  }
}
