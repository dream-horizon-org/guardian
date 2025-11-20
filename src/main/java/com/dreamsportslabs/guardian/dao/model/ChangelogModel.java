package com.dreamsportslabs.guardian.dao.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
  private String tenantId;
  private String configType;
  private String operationType;
  private String changedBy;
  private String changedAt;
  private Map<String, Object> oldValues;
  private Map<String, Object> newValues;
}
