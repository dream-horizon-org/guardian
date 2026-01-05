package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateIntegerRange;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

@Data
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
  private Map<String, String> templateParams;

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateSmsConfigRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(
        req.getIsSslEnabled(),
        req.getHost(),
        req.getPort(),
        req.getSendSmsPath(),
        req.getTemplateName(),
        req.getTemplateParams());

    validateString(req.getHost(), "host", 256, true);
    validateIntegerRange(req.getPort(), "port", 1, 65535);
    validateString(req.getSendSmsPath(), "send_sms_path", 256, true);
    validateString(req.getTemplateName(), "template_name", 256, true);
  }
}
