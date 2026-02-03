package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.EmailConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EmailConfigResponseDto {
  private String tenantId;
  private Boolean isSslEnabled;
  private String host;
  private Integer port;
  private String sendEmailPath;
  private String templateName;
  private HashMap<String, String> templateParams;

  public static EmailConfigResponseDto from(String tenantId, EmailConfigModel model) {
    return EmailConfigResponseDto.builder()
        .tenantId(tenantId)
        .isSslEnabled(model.getIsSslEnabled())
        .host(model.getHost())
        .port(model.getPort())
        .sendEmailPath(model.getSendEmailPath())
        .templateName(model.getTemplateName())
        .templateParams(model.getTemplateParams())
        .build();
  }
}
