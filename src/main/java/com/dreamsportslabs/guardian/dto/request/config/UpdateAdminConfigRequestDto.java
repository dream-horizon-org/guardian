package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.Utils.requireNonBlankIfPresent;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateAdminConfigRequestDto {
  @Size(max = 50, message = "username cannot exceed 50 characters")
  private String username;

  @Size(max = 50, message = "password cannot exceed 50 characters")
  private String password;

  public void validate() {
    requireAtLeastOneField(username, password);
    requireNonBlankIfPresent(username, "username");
    requireNonBlankIfPresent(password, "password");
  }
}
