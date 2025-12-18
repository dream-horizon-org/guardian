package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UpdateEmailConfigRequestDto {
  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  private String host;

  private Integer port;

  @JsonProperty("send_email_path")
  private String sendEmailPath;

  @JsonProperty("template_name")
  private String templateName;

  @JsonProperty("template_params")
  private java.util.Map<String, String> templateParams;

  public void validate() {
    boolean hasFields = false;

    if (isSslEnabled != null) {
      hasFields = true;
    }

    if (host != null) {
      hasFields = true;
      if (StringUtils.isBlank(host)) {
        throw INVALID_REQUEST.getCustomException("host cannot be blank");
      }
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

    if (sendEmailPath != null) {
      hasFields = true;
      if (StringUtils.isBlank(sendEmailPath)) {
        throw INVALID_REQUEST.getCustomException("send_email_path cannot be blank");
      }
      if (sendEmailPath.length() > 256) {
        throw INVALID_REQUEST.getCustomException("send_email_path cannot exceed 256 characters");
      }
    }

    if (templateName != null) {
      hasFields = true;
      if (StringUtils.isBlank(templateName)) {
        throw INVALID_REQUEST.getCustomException("template_name cannot be blank");
      }
      if (templateName.length() > 256) {
        throw INVALID_REQUEST.getCustomException("template_name cannot exceed 256 characters");
      }
    }

    if (templateParams != null) {
      hasFields = true;
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
