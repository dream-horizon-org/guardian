package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.Utils.requireNonBlankIfPresent;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import lombok.Getter;

@Getter
public class UpdateEmailConfigRequestDto {
  @Size(max = 256, message = "host cannot exceed 256 characters")
  private String host;

  @Min(value = 1, message = "port must be greater than or equal to 1")
  @Max(value = 65535, message = "port must be less than or equal to 65535")
  private Integer port;

  @JsonProperty("send_email_path")
  @Size(max = 256, message = "send_email_path cannot exceed 256 characters")
  private String sendEmailPath;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("template_name")
  @Size(max = 256, message = "template_name cannot exceed 256 characters")
  private String templateName;

  @JsonProperty("template_params")
  private HashMap<String, String> templateParams;

  public void validate() {
    requireAtLeastOneField(host, port, sendEmailPath, isSslEnabled, templateName, templateParams);

    requireNonBlankIfPresent(host, "host");
    requireNonBlankIfPresent(sendEmailPath, "send_email_path");
    requireNonBlankIfPresent(templateName, "template_name");
  }
}
