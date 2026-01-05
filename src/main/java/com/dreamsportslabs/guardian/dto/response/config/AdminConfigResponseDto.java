package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.AdminConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class AdminConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  private String username;

  private String password;

  public static AdminConfigResponseDto from(AdminConfigModel model) {
    return AdminConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .username(model.getUsername())
        .password(model.getPassword())
        .build();
  }
}
