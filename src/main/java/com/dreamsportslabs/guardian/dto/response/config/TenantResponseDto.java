package com.dreamsportslabs.guardian.dto.response.config;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class TenantResponseDto {
  private String id;
  private String name;
}
