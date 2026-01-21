package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.dreamsportslabs.guardian.validation.annotation.NotBlankIfPresent;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateSmsConfigRequestDto {
  private Boolean isSslEnabled;

  @NotBlankIfPresent(message = "host cannot be blank")
  @Size(max = 256, message = "host cannot exceed 256 characters")
  private String host;

  @Min(value = 1, message = "port must be greater than or equal to 1")
  @Max(value = 65535, message = "port must be less than or equal to 65535")
  private Integer port;

  @NotBlankIfPresent(message = "send_sms_path cannot be blank")
  @Size(max = 256, message = "send_sms_path cannot exceed 256 characters")
  private String sendSmsPath;

  @NotBlankIfPresent(message = "template_name cannot be blank")
  @Size(max = 256, message = "template_name cannot exceed 256 characters")
  private String templateName;

  private HashMap<String, String> templateParams;

  public void validate() {
    requireAtLeastOneField(isSslEnabled, host, port, sendSmsPath, templateName, templateParams);
  }
}
