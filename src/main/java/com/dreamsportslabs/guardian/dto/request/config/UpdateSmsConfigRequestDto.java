package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateIntegerRange;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import lombok.Getter;

@Getter
public class UpdateSmsConfigRequestDto {
  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  private String host;
  private Integer port;

  @JsonProperty("send_sms_path")
  private String sendSmsPath;

  @JsonProperty("template_name")
  private String templateName;

  @JsonProperty("template_params")
  private HashMap<String, String> templateParams;

  public void validate() {
    requireAtLeastOneField(isSslEnabled, host, port, sendSmsPath, templateName, templateParams);
    validateString(host, "host", 256, false);
    validateIntegerRange(port, "port", 1, 65535, false);
    validateString(sendSmsPath, "send_sms_path", 256, false);
    validateString(templateName, "template_name", 256, false);
  }
}
