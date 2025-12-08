package com.dreamsportslabs.guardian.dto.response.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public class EmailConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  @JsonProperty("host")
  private String host;

  @JsonProperty("port")
  private Integer port;

  @JsonProperty("send_email_path")
  private String sendEmailPath;

  @JsonProperty("template_name")
  private String templateName;

  @JsonProperty("template_params")
  private Map<String, String> templateParams;
}
