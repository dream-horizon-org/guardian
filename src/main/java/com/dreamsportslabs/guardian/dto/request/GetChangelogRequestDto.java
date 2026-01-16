package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateInteger;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateIntegerRange;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;

@Getter
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
    validateString(tenantId, "tenant_id", 256, true);
    validateIntegerRange(Integer.valueOf(limit), "limit", 1, 100, false);
    validateInteger(Integer.valueOf(offset), "offset", 0, false);
  }
}
