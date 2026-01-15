package com.dreamsportslabs.guardian.dao.model.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Setter
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuestConfigModel {
  private Boolean isEncrypted;
  private String secretKey;
  private List<String> allowedScopes;
}
