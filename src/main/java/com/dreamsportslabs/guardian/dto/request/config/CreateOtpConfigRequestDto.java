package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateInteger;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequired;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateRequiredString;
import static com.dreamsportslabs.guardian.utils.Utils.WhitelistedInputsDeserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import lombok.Data;

@Data
public class CreateOtpConfigRequestDto {
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
    validateRequiredString(tenantId, "tenant_id", 10);
    validateInteger(otpLength, "otp_length", 1);
    validateInteger(tryLimit, "try_limit", 1);
    validateInteger(resendLimit, "resend_limit", 1);
    validateInteger(otpResendInterval, "otp_resend_interval", 1);
    validateInteger(otpValidity, "otp_validity", 1);
    validateRequired(whitelistedInputs, "whitelisted_inputs");
  }
}
