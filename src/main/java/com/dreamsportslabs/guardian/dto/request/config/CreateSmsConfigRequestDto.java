package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateSmsConfigRequestDto {
  @JsonProperty("tenant_id")
  private String tenantId;

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
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant_id is required");
    }

    if (StringUtils.isBlank(host)) {
      throw INVALID_REQUEST.getCustomException("host is required");
    }

    if (host.length() > 256) {
      throw INVALID_REQUEST.getCustomException("host cannot exceed 256 characters");
    }

    if (port == null) {
      throw INVALID_REQUEST.getCustomException("port is required");
    }

    if (port < 1 || port > 65535) {
      throw INVALID_REQUEST.getCustomException("port must be between 1 and 65535");
    }

    if (StringUtils.isBlank(sendSmsPath)) {
      throw INVALID_REQUEST.getCustomException("send_sms_path is required");
    }

    if (sendSmsPath.length() > 256) {
      throw INVALID_REQUEST.getCustomException("send_sms_path cannot exceed 256 characters");
    }

    if (StringUtils.isBlank(templateName)) {
      throw INVALID_REQUEST.getCustomException("template_name is required");
    }

    if (templateName.length() > 256) {
      throw INVALID_REQUEST.getCustomException("template_name cannot exceed 256 characters");
    }

    if (templateParams == null || templateParams.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("template_params is required and cannot be empty");
    }
  }
}
