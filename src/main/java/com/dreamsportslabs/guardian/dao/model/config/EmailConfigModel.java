package com.dreamsportslabs.guardian.dao.model.config;

import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class EmailConfigModel {
  private String host;
  private Integer port;
  private String sendEmailPath;
  private Boolean isSslEnabled;
  private String templateName;
  private HashMap<String, String> templateParams;
}
