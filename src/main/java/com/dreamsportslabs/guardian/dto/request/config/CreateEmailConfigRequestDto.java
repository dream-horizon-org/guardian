package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateIntegerRange;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequired;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequiredString;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

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
  private Map<String, String> templateParams;

  public void validate() {
    validateRequiredString(tenantId, "tenant_id", 10);
    validateRequiredString(host, "host", 256);
    validateIntegerRange(port, "port", 1, 65535);
    validateRequiredString(sendEmailPath, "send_email_path", 256);
    validateRequiredString(templateName, "template_name", 256);
    validateRequired(templateParams, "template_params");
  }
}
