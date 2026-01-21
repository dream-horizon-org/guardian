package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.SmsConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SmsConfigResponseDto {
  private String tenantId;
  private Boolean isSslEnabled;
  private String host;
  private Integer port;
  private String sendSmsPath;
  private String templateName;
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
