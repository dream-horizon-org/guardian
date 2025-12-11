package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateEmailConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

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
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot be blank");
    }
    if (tenantId.length() > 10) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot exceed 10 characters");
    }

    if (StringUtils.isBlank(host)) {
      throw INVALID_REQUEST.getCustomException("host cannot be blank");
    }
    if (host.length() > 256) {
      throw INVALID_REQUEST.getCustomException("host cannot exceed 256 characters");
    }

    if (port != null && (port < 1 || port > 65535)) {
      throw INVALID_REQUEST.getCustomException("port must be between 1 and 65535");
    }

    if (StringUtils.isBlank(sendEmailPath)) {
      throw INVALID_REQUEST.getCustomException("send_email_path cannot be blank");
    }
    if (sendEmailPath.length() > 256) {
      throw INVALID_REQUEST.getCustomException("send_email_path cannot exceed 256 characters");
    }

    if (StringUtils.isBlank(templateName)) {
      throw INVALID_REQUEST.getCustomException("template_name cannot be blank");
    }
    if (templateName.length() > 256) {
      throw INVALID_REQUEST.getCustomException("template_name cannot exceed 256 characters");
    }

    if (templateParams == null) {
      throw INVALID_REQUEST.getCustomException("template_params cannot be null");
    }
  }
}
