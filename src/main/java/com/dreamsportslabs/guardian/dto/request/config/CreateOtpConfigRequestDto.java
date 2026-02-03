package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_OTP_MOCKED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_LENGTH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_RESEND_INTERVAL;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_SEND_BLOCK_SECONDS;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_SEND_WINDOW_MAX_COUNT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_SEND_WINDOW_SECONDS;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_VALIDITY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_RESEND_LIMIT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TRY_LIMIT;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateOtpConfigRequestDto {
  @NotNull private Boolean isOtpMocked = DEFAULT_IS_OTP_MOCKED;

  @NotNull
  @Min(value = 1, message = "otp_length must be greater than or equal to 1")
  private Integer otpLength = DEFAULT_OTP_LENGTH;

  @NotNull
  @Min(value = 1, message = "try_limit must be greater than or equal to 1")
  private Integer tryLimit = DEFAULT_TRY_LIMIT;

  @NotNull
  @Min(value = 1, message = "resend_limit must be greater than or equal to 1")
  private Integer resendLimit = DEFAULT_RESEND_LIMIT;

  @NotNull
  @Min(value = 1, message = "otp_resend_interval must be greater than or equal to 1")
  private Integer otpResendInterval = DEFAULT_OTP_RESEND_INTERVAL;

  @NotNull
  @Min(value = 1, message = "otp_validity must be greater than or equal to 1")
  private Integer otpValidity = DEFAULT_OTP_VALIDITY;

  @NotNull
  @Min(value = 1, message = "otp_send_window_seconds must be greater than or equal to 1")
  private Integer otpSendWindowSeconds = DEFAULT_OTP_SEND_WINDOW_SECONDS;

  @NotNull
  @Min(value = 1, message = "otp_send_window_max_count must be greater than or equal to 1")
  private Integer otpSendWindowMaxCount = DEFAULT_OTP_SEND_WINDOW_MAX_COUNT;

  @NotNull
  @Min(value = 1, message = "otp_send_block_seconds must be greater than or equal to 1")
  private Integer otpSendBlockSeconds = DEFAULT_OTP_SEND_BLOCK_SECONDS;

  @NotNull(message = "whitelisted_inputs cannot be null")
  private HashMap<String, String> whitelistedInputs;
}
