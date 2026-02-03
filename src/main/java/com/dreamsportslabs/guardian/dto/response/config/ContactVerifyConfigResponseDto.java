package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.config.ContactVerifyConfigModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ContactVerifyConfigResponseDto {
  private String tenantId;
  private Boolean isOtpMocked;
  private Integer otpLength;
  private Integer tryLimit;
  private Integer resendLimit;
  private Integer otpResendInterval;
  private Integer otpValidity;
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
