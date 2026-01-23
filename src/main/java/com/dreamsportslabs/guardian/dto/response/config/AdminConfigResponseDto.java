package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.AdminConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdminConfigResponseDto {
  private String tenantId;
  private String username;
  private String password;

  public static AdminConfigResponseDto from(String tenantId, AdminConfigModel model) {
    return AdminConfigResponseDto.builder()
        .tenantId(tenantId)
        .username(model.getUsername())
        .password(model.getPassword())
        .build();
  }
}
