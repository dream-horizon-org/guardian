package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.AuthCodeConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class AuthCodeConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  private Integer ttl;

  private Integer length;

  public static AuthCodeConfigResponseDto from(AuthCodeConfigModel model) {
    return AuthCodeConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .ttl(model.getTtl())
        .length(model.getLength())
        .build();
  }
}
