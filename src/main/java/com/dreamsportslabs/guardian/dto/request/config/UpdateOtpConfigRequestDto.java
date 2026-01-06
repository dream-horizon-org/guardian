package com.dreamsportslabs.guardian.dto.request.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.requireAtLeastOneField;
import static com.dreamsportslabs.guardian.utils.DtoValidationUtil.validateInteger;
import static com.dreamsportslabs.guardian.utils.Utils.WhitelistedInputsDeserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import lombok.Data;

@Data
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

  @JsonProperty("whitelisted_inputs")
  @JsonDeserialize(using = WhitelistedInputsDeserializer.class)
  private List<String> whitelistedInputs;

  public void validate() {
    validate(this);
  }

  public static void validate(UpdateOtpConfigRequestDto req) {
    if (req == null) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    requireAtLeastOneField(
        req.getIsOtpMocked(),
        req.getOtpLength(),
        req.getTryLimit(),
        req.getResendLimit(),
        req.getOtpResendInterval(),
        req.getOtpValidity(),
        req.getWhitelistedInputs());

    validateInteger(req.getOtpLength(), "otp_length", 1);
    validateInteger(req.getTryLimit(), "try_limit", 1);
    validateInteger(req.getResendLimit(), "resend_limit", 1);
    validateInteger(req.getOtpResendInterval(), "otp_resend_interval", 1);
    validateInteger(req.getOtpValidity(), "otp_validity", 1);
  }
}
