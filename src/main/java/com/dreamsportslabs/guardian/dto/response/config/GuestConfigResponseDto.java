package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.GuestConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GuestConfigResponseDto {
  private String tenantId;
  private Boolean isEncrypted;
  private String secretKey;
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
