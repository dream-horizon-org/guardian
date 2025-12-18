package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class GetChangelogRequestDto {
  @QueryParam("tenant_id")
  private String tenantId;

  @QueryParam("limit")
  @DefaultValue("50")
  private int limit;

  @QueryParam("offset")
  @DefaultValue("0")
  private int offset;

  public void validate() {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant_id is required");
    }
    if (limit < 1 || limit > 100) {
      throw INVALID_REQUEST.getCustomException("limit must be between 1 and 100");
    }
    if (offset < 0) {
      throw INVALID_REQUEST.getCustomException("offset must be greater than or equal to 0");
    }
  }
}
