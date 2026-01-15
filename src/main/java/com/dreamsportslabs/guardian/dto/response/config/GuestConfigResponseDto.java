package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.GuestConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GuestConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

  @JsonProperty("is_encrypted")
  private Boolean isEncrypted;

  @JsonProperty("secret_key")
  private String secretKey;

  @JsonProperty("allowed_scopes")
  private List<String> allowedScopes;

  public static GuestConfigResponseDto from(String tenantId, GuestConfigModel model) {
    return GuestConfigResponseDto.builder()
        .tenantId(tenantId)
        .isEncrypted(model.getIsEncrypted())
        .secretKey(model.getSecretKey())
        .allowedScopes(model.getAllowedScopes())
        .build();
  }
}
