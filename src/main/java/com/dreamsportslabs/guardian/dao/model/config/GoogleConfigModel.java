package com.dreamsportslabs.guardian.dao.model.config;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class GoogleConfigModel {
  private String clientId;
  private String clientSecret;
}
