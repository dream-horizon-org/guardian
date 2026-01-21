package com.dreamsportslabs.guardian.dao.model.config;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class AuthCodeConfigModel {
  private Integer ttl;
  private Integer length;
}
