package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.dreamsportslabs.guardian.constant.ClientAuthMethod;
import com.dreamsportslabs.guardian.validation.annotation.NotBlankIfPresent;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateOidcProviderConfigRequestDto {
  @Size(max = 256, message = "issuer cannot exceed 256 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "issuer must be a valid HTTP/HTTPS URL")
  private String issuer;

  @Size(max = 256, message = "jwks_url cannot exceed 256 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "jwks_url must be a valid HTTP/HTTPS URL")
  private String jwksUrl;

  @Size(max = 256, message = "token_url cannot exceed 256 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "token_url must be a valid HTTP/HTTPS URL")
  private String tokenUrl;

  @NotBlankIfPresent(message = "client_id cannot be blank")
  @Size(max = 256, message = "client_id cannot exceed 256 characters")
  private String clientId;

  @NotBlankIfPresent(message = "client_secret cannot be blank")
  @Size(max = 256, message = "client_secret cannot exceed 256 characters")
  private String clientSecret;

  @Size(max = 256, message = "redirect_uri cannot exceed 256 characters")
  @Pattern(
      regexp = "^https?://[\\w\\-]+(\\.[\\w\\-]+)*(:[0-9]+)?(/.*)?$",
      message = "redirect_uri must be a valid HTTP/HTTPS URL")
  private String redirectUri;

  private ClientAuthMethod clientAuthMethod;

  private Boolean isSslEnabled;

  @Size(max = 20, message = "user_identifier cannot exceed 20 characters")
  private String userIdentifier;

  private Map<String, Object> audienceClaims;

  public void validate() {
    requireAtLeastOneField(
        issuer,
        jwksUrl,
        tokenUrl,
        clientId,
        clientSecret,
        redirectUri,
        clientAuthMethod,
        isSslEnabled,
        userIdentifier,
        audienceClaims);
  }
}
