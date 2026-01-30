package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.AuthCodeConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthCodeConfigResponseDto {
  private String tenantId;
  private Integer ttl;
  private Integer length;

  public static AuthCodeConfigResponseDto from(String tenantId, AuthCodeConfigModel model) {
    return AuthCodeConfigResponseDto.builder()
        .tenantId(tenantId)
        .ttl(model.getTtl())
        .length(model.getLength())
        .build();
  }
}
