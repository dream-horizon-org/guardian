package com.dreamsportslabs.guardian.dto.response.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public class TenantResponseDto {
  @JsonProperty("id")
  private String tenantId;

  @JsonProperty("name")
  private String name;
}
