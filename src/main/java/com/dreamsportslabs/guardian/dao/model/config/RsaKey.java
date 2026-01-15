package com.dreamsportslabs.guardian.dao.model.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Setter
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RsaKey {
  @JsonProperty("public_key")
  private String publicKey;

  @JsonProperty("private_key")
  private String privateKey;

  private String kid;

  @Builder.Default
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private Boolean current = false;
}
