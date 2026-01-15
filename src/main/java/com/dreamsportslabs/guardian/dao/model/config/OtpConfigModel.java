package com.dreamsportslabs.guardian.dao.model.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Setter
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
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
