package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.OtpConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OtpConfigResponseDto {
  @JsonProperty("tenant_id")
  private String tenantId;

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

  public static OtpConfigResponseDto from(String tenantId, OtpConfigModel model) {
    return OtpConfigResponseDto.builder()
        .tenantId(tenantId)
        .isOtpMocked(model.getIsOtpMocked())
        .otpLength(model.getOtpLength())
        .tryLimit(model.getTryLimit())
        .resendLimit(model.getResendLimit())
        .otpResendInterval(model.getOtpResendInterval())
        .otpValidity(model.getOtpValidity())
        .otpSendWindowSeconds(model.getOtpSendWindowSeconds())
        .otpSendWindowMaxCount(model.getOtpSendWindowMaxCount())
        .otpSendBlockSeconds(model.getOtpSendBlockSeconds())
        .whitelistedInputs(model.getWhitelistedInputs())
        .build();
  }
}
