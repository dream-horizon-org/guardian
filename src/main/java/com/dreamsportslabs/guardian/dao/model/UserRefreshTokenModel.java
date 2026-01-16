package com.dreamsportslabs.guardian.dao.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserRefreshTokenModel {
  private String tenantId;
  private String clientId;
  private String refreshToken;
  private String deviceName;
  private String location;
  private String ip;
  private String source;
}
