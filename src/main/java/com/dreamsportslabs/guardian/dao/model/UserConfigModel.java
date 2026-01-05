package com.dreamsportslabs.guardian.dao.model;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_SEND_PROVIDER_DETAILS;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_PORT;

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

  @Builder.Default private Boolean isSslEnabled = DEFAULT_IS_SSL_ENABLED;

  private String host;

  @Builder.Default private Integer port = DEFAULT_USER_CONFIG_PORT;

  private String getUserPath;
  private String createUserPath;
  private String authenticateUserPath;
  private String addProviderPath;

  @Builder.Default private Boolean sendProviderDetails = DEFAULT_SEND_PROVIDER_DETAILS;
}
