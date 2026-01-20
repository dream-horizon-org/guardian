package com.dreamsportslabs.guardian.dto.request.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateGoogleConfigRequestDto {
  @JsonProperty("client_id")
  @NotBlank(message = "client_id cannot be blank")
  @Size(max = 256, message = "client_id cannot exceed 256 characters")
  private String clientId;

  @JsonProperty("client_secret")
  @NotBlank(message = "client_secret cannot be blank")
  @Size(max = 256, message = "client_secret cannot exceed 256 characters")
  private String clientSecret;
}
