package com.dreamsportslabs.guardian.dao.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Setter
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailConfigModel {
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
