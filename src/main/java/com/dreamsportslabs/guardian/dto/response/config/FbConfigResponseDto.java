package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.FbConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class FbConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("app_id")
  private String appId;

  @JsonProperty("app_secret")
  private String appSecret;

  @JsonProperty("send_app_secret")
  private Boolean sendAppSecret;

  public static FbConfigResponseDto from(FbConfigModel model) {
    return FbConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .appId(model.getAppId())
        .appSecret(model.getAppSecret())
        .sendAppSecret(model.getSendAppSecret())
        .build();
  }
}
