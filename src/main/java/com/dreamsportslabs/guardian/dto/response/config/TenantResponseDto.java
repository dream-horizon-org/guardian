package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.TenantModel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class TenantResponseDto {
  private String id;
  private String name;

  public static TenantResponseDto from(TenantModel model) {
    return TenantResponseDto.builder().id(model.getId()).name(model.getName()).build();
  }
}
