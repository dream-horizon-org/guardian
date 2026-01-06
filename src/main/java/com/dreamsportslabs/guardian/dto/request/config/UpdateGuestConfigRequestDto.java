package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateString;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class UpdateGuestConfigRequestDto {
  @JsonProperty("is_encrypted")
  private Boolean isEncrypted;

  @JsonProperty("secret_key")
  private String secretKey;

  @JsonProperty("allowed_scopes")
  private List<String> allowedScopes;

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateGuestConfigRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(req.getIsEncrypted(), req.getSecretKey(), req.getAllowedScopes());

    validateString(req.getSecretKey(), "secret_key", 16, false);
  }
}
