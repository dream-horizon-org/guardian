package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.Utils.WhitelistedInputsDeserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import lombok.Data;

@Data
public class UpdateContactVerifyConfigRequestDto {
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
    boolean hasFields = false;

    if (isOtpMocked != null) {
      hasFields = true;
    }

    if (otpLength != null) {
      hasFields = true;
      if (otpLength < 1) {
        throw INVALID_REQUEST.getCustomException("otp_length must be greater than 0");
      }
    }

    if (tryLimit != null) {
      hasFields = true;
      if (tryLimit < 1) {
        throw INVALID_REQUEST.getCustomException("try_limit must be greater than 0");
      }
    }

    if (resendLimit != null) {
      hasFields = true;
      if (resendLimit < 1) {
        throw INVALID_REQUEST.getCustomException("resend_limit must be greater than 0");
      }
    }

    if (otpResendInterval != null) {
      hasFields = true;
      if (otpResendInterval < 1) {
        throw INVALID_REQUEST.getCustomException("otp_resend_interval must be greater than 0");
      }
    }

    if (otpValidity != null) {
      hasFields = true;
      if (otpValidity < 1) {
        throw INVALID_REQUEST.getCustomException("otp_validity must be greater than 0");
      }
    }

    if (whitelistedInputs != null) {
      hasFields = true;
    }

    if (!hasFields) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }
}
