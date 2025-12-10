package com.dreamsportslabs.guardian.dto.response.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class UserConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  private String host;

  private Integer port;

  @JsonProperty("get_user_path")
  private String getUserPath;

  @JsonProperty("create_user_path")
  private String createUserPath;

  @JsonProperty("authenticate_user_path")
  private String authenticateUserPath;

  @JsonProperty("add_provider_path")
  private String addProviderPath;

  @JsonProperty("send_provider_details")
  private Boolean sendProviderDetails;
}
