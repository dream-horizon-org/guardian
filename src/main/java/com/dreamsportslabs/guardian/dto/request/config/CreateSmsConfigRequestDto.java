package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_SSL_ENABLED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateSmsConfigRequestDto {
  @NotNull private Boolean isSslEnabled = DEFAULT_IS_SSL_ENABLED;

  @NotBlank(message = "host cannot be blank")
  @Size(max = 256, message = "host cannot exceed 256 characters")
  private String host;

  @NotNull(message = "port cannot be null")
  @Min(value = 1, message = "port must be greater than or equal to 1")
  @Max(value = 65535, message = "port must be less than or equal to 65535")
  private Integer port;

  @NotBlank(message = "send_sms_path cannot be blank")
  @Size(max = 256, message = "send_sms_path cannot exceed 256 characters")
  private String sendSmsPath;

  @NotBlank(message = "template_name cannot be blank")
  @Size(max = 256, message = "template_name cannot exceed 256 characters")
  private String templateName;

  @NotNull(message = "template_params cannot be null")
  private HashMap<String, String> templateParams;
}
