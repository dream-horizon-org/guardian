package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateContactVerifyConfigRequestDto {
  private Boolean isOtpMocked;

  @Min(value = 1, message = "otp_length must be greater than or equal to 1")
  private Integer otpLength;

  @Min(value = 1, message = "try_limit must be greater than or equal to 1")
  private Integer tryLimit;

  @Min(value = 1, message = "resend_limit must be greater than or equal to 1")
  private Integer resendLimit;

  @Min(value = 1, message = "otp_resend_interval must be greater than or equal to 1")
  private Integer otpResendInterval;

  @Min(value = 1, message = "otp_validity must be greater than or equal to 1")
  private Integer otpValidity;

  private HashMap<String, String> whitelistedInputs;

  public void validate() {
    requireAtLeastOneField(
        isOtpMocked,
        otpLength,
        tryLimit,
        resendLimit,
        otpResendInterval,
        otpValidity,
        whitelistedInputs);
  }
}
