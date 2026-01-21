package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.UserConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserConfigResponseDto {
  private String tenantId;
  private Boolean isSslEnabled;
  private String host;
  private Integer port;
  private String getUserPath;
  private String createUserPath;
  private String authenticateUserPath;
  private String addProviderPath;
  private String updateUserPath;
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
