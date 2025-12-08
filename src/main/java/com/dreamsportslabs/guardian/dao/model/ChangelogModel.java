package com.dreamsportslabs.guardian.dao.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Setter
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangelogModel {
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
}
