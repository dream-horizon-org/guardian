package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.utils.Utils.WhitelistedInputsDeserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateContactVerifyConfigRequestDto {
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
  @JsonDeserialize(using = WhitelistedInputsDeserializer.class)
  private List<String> whitelistedInputs;

  public void validate() {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot be blank");
    }
    if (tenantId.length() > 10) {
      throw INVALID_REQUEST.getCustomException("tenant_id cannot exceed 10 characters");
    }

    if (otpLength != null && otpLength < 1) {
      throw INVALID_REQUEST.getCustomException("otp_length must be greater than 0");
    }

    if (tryLimit != null && tryLimit < 1) {
      throw INVALID_REQUEST.getCustomException("try_limit must be greater than 0");
    }

    if (resendLimit != null && resendLimit < 1) {
      throw INVALID_REQUEST.getCustomException("resend_limit must be greater than 0");
    }

    if (otpResendInterval != null && otpResendInterval < 1) {
      throw INVALID_REQUEST.getCustomException("otp_resend_interval must be greater than 0");
    }

    if (otpValidity != null && otpValidity < 1) {
      throw INVALID_REQUEST.getCustomException("otp_validity must be greater than 0");
    }

    if (whitelistedInputs == null) {
      throw INVALID_REQUEST.getCustomException("whitelisted_inputs cannot be null");
    }
  }
}
