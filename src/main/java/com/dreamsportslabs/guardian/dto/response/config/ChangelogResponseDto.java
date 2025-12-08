package com.dreamsportslabs.guardian.dto.response.config;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class ChangelogResponseDto {
  private Integer total;
  private List<ChangelogSummaryResponseDto> changes;
}
