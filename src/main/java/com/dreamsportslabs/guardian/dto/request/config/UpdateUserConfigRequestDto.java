package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateIntegerRange;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class UpdateUserConfigRequestDto {
  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  private String host;
  private Integer port;

  @JsonProperty("get_user_path")
  private String getUserPath;

  @JsonProperty("create_user_path")
  private String createUserPath;

  @JsonProperty("authenticate_user_path")
  private String authenticateUserPath;

  @JsonProperty("add_provider_path")
  private String addProviderPath;

  @JsonProperty("update_user_path")
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

    validateString(host, "host", 256, false);
    validateIntegerRange(port, "port", 1, 65535, false);
    validateString(getUserPath, "get_user_path", 256, false);
    validateString(createUserPath, "create_user_path", 256, false);
    validateString(authenticateUserPath, "authenticate_user_path", 256, false);
    validateString(addProviderPath, "add_provider_path", 256, false);
    validateString(updateUserPath, "update_user_path", 256, false);
  }
}
