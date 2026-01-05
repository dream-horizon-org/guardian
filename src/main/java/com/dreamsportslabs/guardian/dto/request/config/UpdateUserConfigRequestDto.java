package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateIntegerRange;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
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

  @JsonProperty("send_provider_details")
  private Boolean sendProviderDetails;

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateUserConfigRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(
        req.getIsSslEnabled(),
        req.getHost(),
        req.getPort(),
        req.getGetUserPath(),
        req.getCreateUserPath(),
        req.getAuthenticateUserPath(),
        req.getAddProviderPath(),
        req.getSendProviderDetails());

    validateString(req.getHost(), "host", 256, true);
    validateIntegerRange(req.getPort(), "port", 1, 65535);
    validateString(req.getGetUserPath(), "get_user_path", 256, true);
    validateString(req.getCreateUserPath(), "create_user_path", 256, true);
    validateString(req.getAuthenticateUserPath(), "authenticate_user_path", 256, true);
    validateString(req.getAddProviderPath(), "add_provider_path", 256, true);
  }
}
