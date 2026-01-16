package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.SmsConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SmsConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

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

  public static SmsConfigResponseDto from(String tenantId, SmsConfigModel model) {
    return SmsConfigResponseDto.builder()
        .tenantId(tenantId)
        .isSslEnabled(model.getIsSslEnabled())
        .host(model.getHost())
        .port(model.getPort())
        .sendSmsPath(model.getSendSmsPath())
        .templateName(model.getTemplateName())
        .templateParams(model.getTemplateParams())
        .build();
  }
}
