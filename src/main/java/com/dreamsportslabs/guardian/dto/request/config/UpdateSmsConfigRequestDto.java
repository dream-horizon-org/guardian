package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UpdateSmsConfigRequestDto {
  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("host")
  private String host;

  @JsonProperty("port")
  private Integer port;

  @JsonProperty("send_sms_path")
  private String sendSmsPath;

  @JsonProperty("template_name")
  private String templateName;

  @JsonProperty("template_params")
  private Map<String, String> templateParams;

  public void validate() {
    boolean hasFields = false;

    if (isSslEnabled != null) {
      hasFields = true;
    }

    if (StringUtils.isNotBlank(host)) {
      hasFields = true;
      if (host.length() > 256) {
        throw INVALID_REQUEST.getCustomException("host cannot exceed 256 characters");
      }
    }

    if (port != null) {
      hasFields = true;
      if (port < 1 || port > 65535) {
        throw INVALID_REQUEST.getCustomException("port must be between 1 and 65535");
      }
    }

    if (StringUtils.isNotBlank(sendSmsPath)) {
      hasFields = true;
      if (sendSmsPath.length() > 256) {
        throw INVALID_REQUEST.getCustomException("send_sms_path cannot exceed 256 characters");
      }
    }

    if (StringUtils.isNotBlank(templateName)) {
      hasFields = true;
      if (templateName.length() > 256) {
        throw INVALID_REQUEST.getCustomException("template_name cannot exceed 256 characters");
      }
    }

    if (templateParams != null) {
      hasFields = true;
      if (templateParams.isEmpty()) {
        throw INVALID_REQUEST.getCustomException("template_params cannot be empty");
      }
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
