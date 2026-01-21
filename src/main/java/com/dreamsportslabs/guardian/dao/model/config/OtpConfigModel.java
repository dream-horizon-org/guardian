package com.dreamsportslabs.guardian.dao.model.config;

import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class OtpConfigModel {
  private Integer otpLength;
  private Integer tryLimit;
  private Boolean isOtpMocked;
  private Integer resendLimit;
  private Integer otpResendInterval;
  private Integer otpValidity;
  private Integer otpSendWindowSeconds;
  private Integer otpSendWindowMaxCount;
  private Integer otpSendBlockSeconds;
  private HashMap<String, String> whitelistedInputs;
}
