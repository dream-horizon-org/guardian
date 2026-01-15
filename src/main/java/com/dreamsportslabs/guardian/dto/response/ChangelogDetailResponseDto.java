package com.dreamsportslabs.guardian.dto.response;

import com.dreamsportslabs.guardian.dao.model.ChangelogModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChangelogDetailResponseDto {
  private Long id;

  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("config_type")
  private String configType;

  @JsonProperty("operation_type")
  private String operationType;

  @JsonProperty("changed_by")
  private String changedBy;

  @JsonProperty("changed_at")
  private String changedAt;

  @JsonProperty("old_values")
  private Map<String, Object> oldValues;

  @JsonProperty("new_values")
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
