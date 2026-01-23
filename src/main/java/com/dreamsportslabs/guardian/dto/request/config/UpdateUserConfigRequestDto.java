package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.dreamsportslabs.guardian.validation.annotation.NotBlankIfPresent;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateUserConfigRequestDto {
  private Boolean isSslEnabled;

  @NotBlankIfPresent(message = "host cannot be blank")
  @Size(max = 256, message = "host cannot exceed 256 characters")
  private String host;

  @Min(value = 1, message = "port must be greater than or equal to 1")
  @Max(value = 65535, message = "port must be less than or equal to 65535")
  private Integer port;

  @NotBlankIfPresent(message = "get_user_path cannot be blank")
  @Size(max = 256, message = "get_user_path cannot exceed 256 characters")
  private String getUserPath;

  @NotBlankIfPresent(message = "create_user_path cannot be blank")
  @Size(max = 256, message = "create_user_path cannot exceed 256 characters")
  private String createUserPath;

  @NotBlankIfPresent(message = "authenticate_user_path cannot be blank")
  @Size(max = 256, message = "authenticate_user_path cannot exceed 256 characters")
  private String authenticateUserPath;

  @NotBlankIfPresent(message = "add_provider_path cannot be blank")
  @Size(max = 256, message = "add_provider_path cannot exceed 256 characters")
  private String addProviderPath;

  @NotBlankIfPresent(message = "update_user_path cannot be blank")
  @Size(max = 256, message = "update_user_path cannot exceed 256 characters")
  private String updateUserPath;

  private Boolean sendProviderDetails;

  public void validate() {
    requireAtLeastOneField(
        isSslEnabled,
        host,
        port,
        getUserPath,
        createUserPath,
        authenticateUserPath,
        addProviderPath,
        updateUserPath,
        sendProviderDetails);
  }
}
