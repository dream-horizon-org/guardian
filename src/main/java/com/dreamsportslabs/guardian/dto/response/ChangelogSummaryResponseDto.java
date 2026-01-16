package com.dreamsportslabs.guardian.dto.response;

import com.dreamsportslabs.guardian.dao.model.ChangelogModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

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

  public static ChangelogSummaryResponseDto from(ChangelogModel model) {
    return ChangelogSummaryResponseDto.builder()
        .id(model.getId())
        .configType(model.getConfigType())
        .operationType(model.getOperationType())
        .changedBy(model.getChangedBy())
        .changedAt(model.getChangedAt())
        .build();
  }
}
