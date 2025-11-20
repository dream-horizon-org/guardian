package com.dreamsportslabs.guardian.dto.response.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class ChangelogSummaryResponseDto {
  private Long id;

  @JsonProperty("config_type")
  private String configType;

  @JsonProperty("operation_type")
  private String operationType;

  @JsonProperty("changed_by")
  private String changedBy;

  @JsonProperty("changed_at")
  private String changedAt;
}
