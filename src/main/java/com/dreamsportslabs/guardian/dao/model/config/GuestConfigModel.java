package com.dreamsportslabs.guardian.dao.model.config;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class GuestConfigModel {
  private Boolean isEncrypted;
  private String secretKey;
  private List<String> allowedScopes;
}
