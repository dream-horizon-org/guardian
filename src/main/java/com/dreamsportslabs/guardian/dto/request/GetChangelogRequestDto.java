package com.dreamsportslabs.guardian.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;

@Getter
public class GetChangelogRequestDto {
  @QueryParam("tenant_id")
  @NotBlank(message = "tenant_id cannot be blank")
  @Size(max = 256, message = "tenant_id cannot exceed 256 characters")
  private String tenantId;

  @QueryParam("limit")
  @DefaultValue("50")
  @Min(value = 1, message = "limit must be greater than or equal to 1")
  @Max(value = 100, message = "limit must be less than or equal to 100")
  private int limit;

  @QueryParam("offset")
  @DefaultValue("0")
  @Min(value = 0, message = "offset must be greater than or equal to 0")
  private int offset;
}
