package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateInteger;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOtpConfigRequestDto {
  @JsonProperty("is_otp_mocked")
  private Boolean isOtpMocked;

  @JsonProperty("otp_length")
  private Integer otpLength;

  @JsonProperty("try_limit")
  private Integer tryLimit;

  @JsonProperty("resend_limit")
  private Integer resendLimit;

  @JsonProperty("otp_resend_interval")
  private Integer otpResendInterval;

  @JsonProperty("otp_validity")
  private Integer otpValidity;

  @JsonProperty("otp_send_window_seconds")
  private Integer otpSendWindowSeconds;

  @JsonProperty("otp_send_window_max_count")
  private Integer otpSendWindowMaxCount;

  @JsonProperty("otp_send_block_seconds")
  private Integer otpSendBlockSeconds;

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
        otpSendWindowSeconds,
        otpSendWindowMaxCount,
        otpSendBlockSeconds,
        whitelistedInputs);

    validateInteger(otpLength, "otp_length", 1, false);
    validateInteger(tryLimit, "try_limit", 1, false);
    validateInteger(resendLimit, "resend_limit", 1, false);
    validateInteger(otpResendInterval, "otp_resend_interval", 1, false);
    validateInteger(otpValidity, "otp_validity", 1, false);
    validateInteger(otpSendWindowSeconds, "otp_send_window_seconds", 1, false);
    validateInteger(otpSendWindowMaxCount, "otp_send_window_max_count", 1, false);
    validateInteger(otpSendBlockSeconds, "otp_send_block_seconds", 1, false);
  }
}
