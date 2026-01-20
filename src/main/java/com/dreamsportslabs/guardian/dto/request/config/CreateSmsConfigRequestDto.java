package com.dreamsportslabs.guardian.dto.request.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import lombok.Getter;

@Getter
public class CreateSmsConfigRequestDto {
  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @NotBlank(message = "host cannot be blank")
  @Size(max = 256, message = "host cannot exceed 256 characters")
  private String host;

  @NotNull(message = "port cannot be null")
  @Min(value = 1, message = "port must be greater than or equal to 1")
  @Max(value = 65535, message = "port must be less than or equal to 65535")
  private Integer port;

  @JsonProperty("send_sms_path")
  @NotBlank(message = "send_sms_path cannot be blank")
  @Size(max = 256, message = "send_sms_path cannot exceed 256 characters")
  private String sendSmsPath;

  @JsonProperty("template_name")
  @NotBlank(message = "template_name cannot be blank")
  @Size(max = 256, message = "template_name cannot exceed 256 characters")
  private String templateName;

  @JsonProperty("template_params")
  @NotNull(message = "template_params cannot be null")
  private HashMap<String, String> templateParams;
}
