package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateAuthCodeConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  private Integer ttl;

  private Integer length;

  public void validate() {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot be blank");
    }
    if (tenantId.length() > 10) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot exceed 10 characters");
    }

    if (ttl == null) {
      throw INVALID_REQUEST.getCustomException("ttl cannot be null");
    }
    if (ttl < 1) {
      throw INVALID_REQUEST.getCustomException("ttl must be greater than 0");
    }

    if (length == null) {
      throw INVALID_REQUEST.getCustomException("length cannot be null");
    }
    if (length < 1) {
      throw INVALID_REQUEST.getCustomException("length must be greater than 0");
    }
  }
}
