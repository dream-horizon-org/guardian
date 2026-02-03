package com.dreamsportslabs.guardian.dto.request.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateTenantRequestDto {
  @NotBlank(message = "id cannot be blank")
  @Size(max = 10, message = "id cannot exceed 10 characters")
  private String id;

  @NotBlank(message = "name cannot be blank")
  @Size(max = 256, message = "name cannot exceed 256 characters")
  private String name;
}
