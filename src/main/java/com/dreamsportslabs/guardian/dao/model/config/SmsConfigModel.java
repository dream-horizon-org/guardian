package com.dreamsportslabs.guardian.dao.model.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmsConfigModel {
  private String host;
  private Integer port;
  private String sendSmsPath;
  private Boolean isSslEnabled;
  private String templateName;
  private HashMap<String, String> templateParams;
}
