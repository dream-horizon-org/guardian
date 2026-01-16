package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.ContactVerifyConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ContactVerifyConfigResponseDto {
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

  @JsonProperty("whitelisted_inputs")
  private HashMap<String, String> whitelistedInputs;

  public static ContactVerifyConfigResponseDto from(
      String tenantId, ContactVerifyConfigModel model) {
    return ContactVerifyConfigResponseDto.builder()
        .tenantId(tenantId)
        .isOtpMocked(model.getIsOtpMocked())
        .otpLength(model.getOtpLength())
        .tryLimit(model.getTryLimit())
        .resendLimit(model.getResendLimit())
        .otpResendInterval(model.getOtpResendInterval())
        .otpValidity(model.getOtpValidity())
        .whitelistedInputs(model.getWhitelistedInputs())
        .build();
  }
}
