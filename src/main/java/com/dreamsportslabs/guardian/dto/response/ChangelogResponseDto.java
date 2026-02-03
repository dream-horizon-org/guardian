package com.dreamsportslabs.guardian.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChangelogResponseDto {
  private Long total;
  private List<ChangelogSummaryResponseDto> changes;
}
