package com.dreamsportslabs.guardian.dto.request.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateAdminConfigRequestDto {
  @NotBlank(message = "username cannot be blank")
  @Size(max = 50, message = "username cannot exceed 50 characters")
  private String username;

  @NotBlank(message = "password cannot be blank")
  @Size(max = 50, message = "password cannot exceed 50 characters")
  private String password;
}
