package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.Utils.requireNonBlankIfPresent;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateGoogleConfigRequestDto {
  @JsonProperty("client_id")
  @Size(max = 256, message = "client_id cannot exceed 256 characters")
  private String clientId;

  @JsonProperty("client_secret")
  @Size(max = 256, message = "client_secret cannot exceed 256 characters")
  private String clientSecret;

  public void validate() {
    requireAtLeastOneField(clientId, clientSecret);

    requireNonBlankIfPresent(clientId, "client_id");
    requireNonBlankIfPresent(clientSecret, "client_secret");
  }
}
