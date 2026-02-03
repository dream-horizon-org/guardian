package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.dreamsportslabs.guardian.dao.model.config.RsaKey;
import com.dreamsportslabs.guardian.validation.annotation.NotBlankIfPresent;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateTokenConfigRequestDto {
  @NotBlankIfPresent(message = "algorithm cannot be blank")
  @Size(max = 10, message = "algorithm cannot exceed 10 characters")
  private String algorithm;

  @NotBlankIfPresent(message = "issuer cannot be blank")
  @Size(max = 256, message = "issuer cannot exceed 256 characters")
  private String issuer;

  private List<RsaKey> rsaKeys;

  @Min(value = 1, message = "access_token_expiry must be greater than or equal to 1")
  private Integer accessTokenExpiry;

  @Min(value = 1, message = "refresh_token_expiry must be greater than or equal to 1")
  private Integer refreshTokenExpiry;

  @Min(value = 1, message = "id_token_expiry must be greater than or equal to 1")
  private Integer idTokenExpiry;

  private List<String> idTokenClaims;

  @NotBlankIfPresent(message = "cookie_same_site cannot be blank")
  @Size(max = 20, message = "cookie_same_site cannot exceed 20 characters")
  private String cookieSameSite;

  @NotBlankIfPresent(message = "cookie_domain cannot be blank")
  @Size(max = 256, message = "cookie_domain cannot exceed 256 characters")
  private String cookieDomain;

  @NotBlankIfPresent(message = "cookie_path cannot be blank")
  @Size(max = 256, message = "cookie_path cannot exceed 256 characters")
  private String cookiePath;

  private Boolean cookieSecure;

  private Boolean cookieHttpOnly;

  private List<String> accessTokenClaims;

  public void validate() {
    requireAtLeastOneField(
        algorithm,
        issuer,
        rsaKeys,
        accessTokenExpiry,
        refreshTokenExpiry,
        idTokenExpiry,
        idTokenClaims,
        cookieSameSite,
        cookieDomain,
        cookiePath,
        cookieSecure,
        cookieHttpOnly,
        accessTokenClaims);
  }
}
