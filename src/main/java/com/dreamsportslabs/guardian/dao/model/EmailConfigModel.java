package com.dreamsportslabs.guardian.dao.model;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_EMAIL_CONFIG_PORT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.utils.Utils.JsonToStringDeserializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
  private String tenantId;

  @Builder.Default private Boolean isSslEnabled = DEFAULT_IS_SSL_ENABLED;

  private String host;

  @Builder.Default private Integer port = DEFAULT_EMAIL_CONFIG_PORT;

  private String sendEmailPath;
  private String templateName;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String templateParams;
}
