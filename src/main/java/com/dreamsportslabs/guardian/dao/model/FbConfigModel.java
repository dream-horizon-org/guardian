package com.dreamsportslabs.guardian.dao.model;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_SEND_APP_SECRET;

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
public class FbConfigModel {
  private String tenantId;
  private String appId;
  private String appSecret;

  @Builder.Default private Boolean sendAppSecret = DEFAULT_SEND_APP_SECRET;
}
