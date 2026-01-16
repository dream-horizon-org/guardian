package com.dreamsportslabs.guardian.dao.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserRefreshTokenModel {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("device_name")
  private String deviceName;

  @JsonProperty("location")
  private String location;

  @JsonProperty("ip")
  private String ip;

  @JsonProperty("source")
  private String source;
}
