package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateGoogleConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("client_secret")
  private String clientSecret;

  public void validate() {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot be blank");
    }
    if (tenantId.length() > 10) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot exceed 10 characters");
    }

    if (StringUtils.isBlank(clientId)) {
      throw INVALID_REQUEST.getCustomException("client_id cannot be blank");
    }
    if (clientId.length() > 256) {
      throw INVALID_REQUEST.getCustomException("client_id cannot exceed 256 characters");
    }

    if (StringUtils.isBlank(clientSecret)) {
      throw INVALID_REQUEST.getCustomException("client_secret cannot be blank");
    }
    if (clientSecret.length() > 256) {
      throw INVALID_REQUEST.getCustomException("client_secret cannot exceed 256 characters");
    }
  }
}
