package com.dreamsportslabs.guardian.dao.model.config;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserConfigModel {
  private String host;
  private Integer port;
  private Boolean isSslEnabled;
  private String createUserPath;
  private String getUserPath;
  private String authenticateUserPath;
  private String addProviderPath;
  private String updateUserPath;
  private Boolean sendProviderDetails;
}
