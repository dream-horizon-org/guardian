package com.dreamsportslabs.guardian.dao.model;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_ENCRYPTED;
import static com.dreamsportslabs.guardian.utils.Utils.JsonToStringDeserializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
  private String tenantId;

  @Builder.Default private Boolean isEncrypted = DEFAULT_IS_ENCRYPTED;

  private String secretKey;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String allowedScopes;
}
