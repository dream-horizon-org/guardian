package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.FbConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FbConfigResponseDto {
  private String tenantId;
  private String appId;
  private String appSecret;
  private Boolean sendAppSecret;

  public static FbConfigResponseDto from(String tenantId, FbConfigModel model) {
    return FbConfigResponseDto.builder()
        .tenantId(tenantId)
        .appId(model.getAppId())
        .appSecret(model.getAppSecret())
        .sendAppSecret(model.getSendAppSecret())
        .build();
  }
}
