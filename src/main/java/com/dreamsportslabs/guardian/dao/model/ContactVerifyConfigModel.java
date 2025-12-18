package com.dreamsportslabs.guardian.dao.model;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_OTP_MOCKED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_LENGTH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_RESEND_INTERVAL;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_VALIDITY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_RESEND_LIMIT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TRY_LIMIT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_WHITELISTED_INPUTS;
import static com.dreamsportslabs.guardian.utils.Utils.JsonToStringDeserializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Setter
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactVerifyConfigModel {
  private String tenantId;

  @Builder.Default private Boolean isOtpMocked = DEFAULT_IS_OTP_MOCKED;

  @Builder.Default private Integer otpLength = DEFAULT_OTP_LENGTH;

  @Builder.Default private Integer tryLimit = DEFAULT_TRY_LIMIT;

  @Builder.Default private Integer resendLimit = DEFAULT_RESEND_LIMIT;

  @Builder.Default private Integer otpResendInterval = DEFAULT_OTP_RESEND_INTERVAL;

  @Builder.Default private Integer otpValidity = DEFAULT_OTP_VALIDITY;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  @Builder.Default
  private String whitelistedInputs = DEFAULT_WHITELISTED_INPUTS;
}
