package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.OtpConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OtpConfigResponseDto {
  private String tenantId;
  private Boolean isOtpMocked;
  private Integer otpLength;
  private Integer tryLimit;
  private Integer resendLimit;
  private Integer otpResendInterval;
  private Integer otpValidity;
  private Integer otpSendWindowSeconds;
  private Integer otpSendWindowMaxCount;
  private Integer otpSendBlockSeconds;
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
