package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.TenantModel;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TenantResponseDto {
  private String id;
  private String name;

  public static TenantResponseDto from(TenantModel model) {
    return TenantResponseDto.builder().id(model.getId()).name(model.getName()).build();
  }
}
