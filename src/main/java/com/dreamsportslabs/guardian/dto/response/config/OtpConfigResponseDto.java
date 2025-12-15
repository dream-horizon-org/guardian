package com.dreamsportslabs.guardian.dto.response.config;

import com.dreamsportslabs.guardian.dao.model.OtpConfigModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonArray;
import java.util.List;
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
  private List<String> whitelistedInputs;

  public static OtpConfigResponseDto from(OtpConfigModel model) {
    JsonArray whitelistedInputsArray = new JsonArray(model.getWhitelistedInputs());
    List<String> whitelistedInputsList =
        new ObjectMapper()
            .convertValue(whitelistedInputsArray.getList(), new TypeReference<List<String>>() {});

    return OtpConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .isOtpMocked(model.getIsOtpMocked())
        .otpLength(model.getOtpLength())
        .tryLimit(model.getTryLimit())
        .resendLimit(model.getResendLimit())
        .otpResendInterval(model.getOtpResendInterval())
        .otpValidity(model.getOtpValidity())
        .whitelistedInputs(whitelistedInputsList)
        .build();
  }
}
