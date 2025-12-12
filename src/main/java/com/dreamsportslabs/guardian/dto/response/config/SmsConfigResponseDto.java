package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.SmsConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
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
  private java.util.Map<String, String> templateParams;

  public static SmsConfigResponseDto from(SmsConfigModel model) {
    JsonObject templateParamsJson = new JsonObject(model.getTemplateParams());
    java.util.Map<String, String> templateParamsMap =
        templateParamsJson.getMap().entrySet().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    java.util.Map.Entry::getKey,
                    entry -> entry.getValue() != null ? entry.getValue().toString() : null));
    return SmsConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .isSslEnabled(model.getIsSslEnabled())
        .host(model.getHost())
        .port(model.getPort())
        .sendSmsPath(model.getSendSmsPath())
        .templateName(model.getTemplateName())
        .templateParams(templateParamsMap)
        .build();
  }
}
