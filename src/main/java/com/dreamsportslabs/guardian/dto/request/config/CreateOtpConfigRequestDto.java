package com.dreamsportslabs.guardian.dto.request.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import lombok.Getter;

@Getter
public class CreateOtpConfigRequestDto {
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

  @JsonProperty("otp_send_window_seconds")
  @Min(value = 1, message = "otp_send_window_seconds must be greater than or equal to 1")
  private Integer otpSendWindowSeconds;

  @JsonProperty("otp_send_window_max_count")
  @Min(value = 1, message = "otp_send_window_max_count must be greater than or equal to 1")
  private Integer otpSendWindowMaxCount;

  @JsonProperty("otp_send_block_seconds")
  @Min(value = 1, message = "otp_send_block_seconds must be greater than or equal to 1")
  private Integer otpSendBlockSeconds;

  @JsonProperty("whitelisted_inputs")
  @NotNull(message = "whitelisted_inputs cannot be null")
  private HashMap<String, String> whitelistedInputs;
}
