package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.GuestConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonArray;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
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

  public static GuestConfigResponseDto from(GuestConfigModel model) {
    return GuestConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .isEncrypted(model.getIsEncrypted())
        .secretKey(model.getSecretKey())
        .allowedScopes(parseJsonArray(model.getAllowedScopes()))
        .build();
  }

  private static List<String> parseJsonArray(String jsonString) {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      return List.of();
    }
    try {
      JsonArray jsonArray = new JsonArray(jsonString);
      return jsonArray.stream()
          .map(item -> item instanceof String ? (String) item : item.toString())
          .toList();
    } catch (Exception e) {
      return List.of();
    }
  }
}
