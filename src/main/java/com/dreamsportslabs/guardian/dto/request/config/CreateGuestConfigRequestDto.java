package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_ENCRYPTED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateGuestConfigRequestDto {
  @NotNull private Boolean isEncrypted = DEFAULT_IS_ENCRYPTED;

  @Size(max = 16, message = "secret_key cannot exceed 16 characters")
  private String secretKey;

  @NotNull private List<String> allowedScopes = new ArrayList<>();
}
