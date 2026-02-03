package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.Utils.requireAtLeastOneField;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateOtpConfigRequestDto {
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

  @Min(value = 1, message = "otp_send_window_seconds must be greater than or equal to 1")
  private Integer otpSendWindowSeconds;

  @Min(value = 1, message = "otp_send_window_max_count must be greater than or equal to 1")
  private Integer otpSendWindowMaxCount;

  @Min(value = 1, message = "otp_send_block_seconds must be greater than or equal to 1")
  private Integer otpSendBlockSeconds;

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
  }
}
