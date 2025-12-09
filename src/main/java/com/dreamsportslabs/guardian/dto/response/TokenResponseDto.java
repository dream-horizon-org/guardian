package com.dreamsportslabs.guardian.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponseDto {
  private String accessToken;
  private String refreshToken;
  private String idToken;
  private String ssoToken;
  private String tokenType;
  private Integer expiresIn;
  private Boolean isNewUser;
  private List<MfaFactorDto> mfaFactors;
}
