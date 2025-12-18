package com.dreamsportslabs.guardian.dto.response.config;

import static com.dreamsportslabs.guardian.utils.Utils.parseWhitelistedInputs;

import com.dreamsportslabs.guardian.dao.model.OtpConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
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

  @JsonProperty("whitelisted_inputs")
  private Map<String, Object> whitelistedInputs;

  public static OtpConfigResponseDto from(OtpConfigModel model) {
    Map<String, Object> whitelistedInputsMap = parseWhitelistedInputs(model.getWhitelistedInputs());

    return OtpConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .isOtpMocked(model.getIsOtpMocked())
        .otpLength(model.getOtpLength())
        .tryLimit(model.getTryLimit())
        .resendLimit(model.getResendLimit())
        .otpResendInterval(model.getOtpResendInterval())
        .otpValidity(model.getOtpValidity())
        .whitelistedInputs(whitelistedInputsMap)
        .build();
  }
}
