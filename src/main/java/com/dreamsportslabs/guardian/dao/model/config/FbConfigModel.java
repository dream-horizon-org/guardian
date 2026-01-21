package com.dreamsportslabs.guardian.dao.model.config;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class FbConfigModel {
  private String appId;
  private String appSecret;
  private Boolean sendAppSecret;
}
