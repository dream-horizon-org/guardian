package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.GoogleConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GoogleConfigResponseDto {
  private String tenantId;
  private String clientId;
  private String clientSecret;

  public static GoogleConfigResponseDto from(String tenantId, GoogleConfigModel model) {
    return GoogleConfigResponseDto.builder()
        .tenantId(tenantId)
        .clientId(model.getClientId())
        .clientSecret(model.getClientSecret())
        .build();
  }
}
