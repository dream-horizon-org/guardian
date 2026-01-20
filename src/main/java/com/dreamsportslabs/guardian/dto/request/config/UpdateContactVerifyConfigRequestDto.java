package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import lombok.Getter;

@Getter
public class UpdateContactVerifyConfigRequestDto {
  @JsonProperty("is_otp_mocked")
  private Boolean isOtpMocked;

  @JsonProperty("otp_length")
  @Min(value = 1, message = "otp_length must be greater than or equal to 1")
  private Integer otpLength;

  @JsonProperty("try_limit")
  @Min(value = 1, message = "try_limit must be greater than or equal to 1")
  private Integer tryLimit;

  @JsonProperty("resend_limit")
  @Min(value = 1, message = "resend_limit must be greater than or equal to 1")
  private Integer resendLimit;

  @JsonProperty("otp_resend_interval")
  @Min(value = 1, message = "otp_resend_interval must be greater than or equal to 1")
  private Integer otpResendInterval;

  @JsonProperty("otp_validity")
  @Min(value = 1, message = "otp_validity must be greater than or equal to 1")
  private Integer otpValidity;

  @JsonProperty("whitelisted_inputs")
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
