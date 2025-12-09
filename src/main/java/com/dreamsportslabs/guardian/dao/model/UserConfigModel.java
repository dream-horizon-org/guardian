package com.dreamsportslabs.guardian.dao.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Setter
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserConfigModel {
  private String tenantId;
  private Boolean isSslEnabled;
  private String host;
  private Integer port;
  private String getUserPath;
  private String createUserPath;
  private String authenticateUserPath;
  private String addProviderPath;
  private Boolean sendProviderDetails;
}
