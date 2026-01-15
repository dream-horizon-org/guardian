package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.GoogleConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GoogleConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("client_secret")
  private String clientSecret;

  public static GoogleConfigResponseDto from(String tenantId, GoogleConfigModel model) {
    return GoogleConfigResponseDto.builder()
        .tenantId(tenantId)
        .clientId(model.getClientId())
        .clientSecret(model.getClientSecret())
        .build();
  }
}
