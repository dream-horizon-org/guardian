package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateIntegerRange;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmailConfigRequestDto {
  private String host;
  private Integer port;

  @JsonProperty("send_email_path")
  private String sendEmailPath;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("template_name")
  private String templateName;

  @JsonProperty("template_params")
  private HashMap<String, String> templateParams;

  public void validate() {
    requireAtLeastOneField(host, port, sendEmailPath, isSslEnabled, templateName, templateParams);

    validateString(host, "host", 256, false);
    validateIntegerRange(port, "port", 1, 65535);
    validateString(sendEmailPath, "send_email_path", 256, false);
    validateString(templateName, "template_name", 256, false);
  }
}
