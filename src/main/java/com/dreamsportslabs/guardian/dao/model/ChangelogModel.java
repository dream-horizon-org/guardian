package com.dreamsportslabs.guardian.dao.model;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
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
