package com.dreamsportslabs.guardian.dto.response;

import com.dreamsportslabs.guardian.dao.model.ChangelogModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChangelogDetailResponseDto {
  private Long id;
  private String tenantId;
  private String configType;
  private String operationType;
  private String changedBy;
  private String changedAt;
  private Map<String, Object> oldValues;
  private Map<String, Object> newValues;

  public static ChangelogDetailResponseDto from(ChangelogModel model) {
    return ChangelogDetailResponseDto.builder()
        .id(model.getId())
        .tenantId(model.getTenantId())
        .configType(model.getConfigType())
        .operationType(model.getOperationType())
        .changedBy(model.getChangedBy())
        .changedAt(model.getChangedAt())
        .oldValues(model.getOldValues())
        .newValues(model.getNewValues())
        .build();
  }
}
