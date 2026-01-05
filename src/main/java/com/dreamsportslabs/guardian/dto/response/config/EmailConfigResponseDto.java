package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.EmailConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class EmailConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  private String host;

  private Integer port;

  @JsonProperty("send_email_path")
  private String sendEmailPath;

  @JsonProperty("template_name")
  private String templateName;

  @JsonProperty("template_params")
  private java.util.Map<String, String> templateParams;

  public static EmailConfigResponseDto from(EmailConfigModel model) {
    JsonObject templateParamsJson = new JsonObject(model.getTemplateParams());
    java.util.Map<String, String> templateParamsMap =
        templateParamsJson.getMap().entrySet().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    java.util.Map.Entry::getKey,
                    entry -> entry.getValue() != null ? entry.getValue().toString() : null));
    return EmailConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .isSslEnabled(model.getIsSslEnabled())
        .host(model.getHost())
        .port(model.getPort())
        .sendEmailPath(model.getSendEmailPath())
        .templateName(model.getTemplateName())
        .templateParams(templateParamsMap)
        .build();
  }
}
