package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UpdateGoogleConfigRequestDto {
  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("client_secret")
  private String clientSecret;

  public void validate() {
    boolean hasFields = false;

    if (clientId != null) {
      hasFields = true;
      if (StringUtils.isBlank(clientId)) {
        throw INVALID_REQUEST.getCustomException("client_id cannot be blank");
      }
      if (clientId.length() > 256) {
        throw INVALID_REQUEST.getCustomException("client_id cannot exceed 256 characters");
      }
    }

    if (clientSecret != null) {
      hasFields = true;
      if (StringUtils.isBlank(clientSecret)) {
        throw INVALID_REQUEST.getCustomException("client_secret cannot be blank");
      }
      if (clientSecret.length() > 256) {
        throw INVALID_REQUEST.getCustomException("client_secret cannot exceed 256 characters");
      }
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
