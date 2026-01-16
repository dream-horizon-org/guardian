package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.UserConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("is_ssl_enabled")
  private Boolean isSslEnabled;

  private String host;

  private Integer port;

  @JsonProperty("get_user_path")
  private String getUserPath;

  @JsonProperty("create_user_path")
  private String createUserPath;

  @JsonProperty("authenticate_user_path")
  private String authenticateUserPath;

  @JsonProperty("add_provider_path")
  private String addProviderPath;

  @JsonProperty("update_user_path")
  private String updateUserPath;

  @JsonProperty("send_provider_details")
  private Boolean sendProviderDetails;

  public static UserConfigResponseDto from(String tenantId, UserConfigModel model) {
    return UserConfigResponseDto.builder()
        .tenantId(tenantId)
        .isSslEnabled(model.getIsSslEnabled())
        .host(model.getHost())
        .port(model.getPort())
        .getUserPath(model.getGetUserPath())
        .createUserPath(model.getCreateUserPath())
        .authenticateUserPath(model.getAuthenticateUserPath())
        .addProviderPath(model.getAddProviderPath())
        .updateUserPath(model.getUpdateUserPath())
        .sendProviderDetails(model.getSendProviderDetails())
        .build();
  }
}
