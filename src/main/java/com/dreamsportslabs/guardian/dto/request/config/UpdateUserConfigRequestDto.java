package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.Utils.requireNonBlankIfPresent;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateUserConfigRequestDto {
  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @Size(max = 256, message = "host cannot exceed 256 characters")
  private String host;

  @Min(value = 1, message = "port must be greater than or equal to 1")
  @Max(value = 65535, message = "port must be less than or equal to 65535")
  private Integer port;

  @JsonProperty("get_user_path")
  @Size(max = 256, message = "get_user_path cannot exceed 256 characters")
  private String getUserPath;

  @JsonProperty("create_user_path")
  @Size(max = 256, message = "create_user_path cannot exceed 256 characters")
  private String createUserPath;

  @JsonProperty("authenticate_user_path")
  @Size(max = 256, message = "authenticate_user_path cannot exceed 256 characters")
  private String authenticateUserPath;

  @JsonProperty("add_provider_path")
  @Size(max = 256, message = "add_provider_path cannot exceed 256 characters")
  private String addProviderPath;

  @JsonProperty("update_user_path")
  @Size(max = 256, message = "update_user_path cannot exceed 256 characters")
  private String updateUserPath;

  @JsonProperty("send_provider_details")
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

    requireNonBlankIfPresent(host, "host");
    requireNonBlankIfPresent(getUserPath, "get_user_path");
    requireNonBlankIfPresent(createUserPath, "create_user_path");
    requireNonBlankIfPresent(authenticateUserPath, "authenticate_user_path");
    requireNonBlankIfPresent(addProviderPath, "add_provider_path");
    requireNonBlankIfPresent(updateUserPath, "update_user_path");
  }
}
