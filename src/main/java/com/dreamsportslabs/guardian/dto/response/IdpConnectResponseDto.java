package com.dreamsportslabs.guardian.dto.response;

import static com.dreamsportslabs.guardian.constant.Constants.APP_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.APP_CODE;
import static com.dreamsportslabs.guardian.constant.Constants.APP_ID_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.APP_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.APP_TOKEN_CODE_EXPIRY;
import static com.dreamsportslabs.guardian.constant.Constants.APP_TOKEN_TYPE;
import static com.dreamsportslabs.guardian.constant.Constants.MFA_FACTOR;
import static com.dreamsportslabs.guardian.constant.Constants.MFA_FACTORS;
import static com.dreamsportslabs.guardian.constant.Constants.MFA_IS_ENABLED;

import com.dreamsportslabs.guardian.dao.model.IdpCredentials;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdpConnectResponseDto {
  private String code;
  private String accessToken;
  private String refreshToken;
  private String idToken;
  private String tokenType;
  private Integer expiresIn;
  private Boolean isNewUser;
  private IdpCredentials idpCredentials;
  private List<MfaFactorDto> mfaFactors;

  public static IdpConnectResponseDto buildIdpConnectResponse(
      Object authResponse, Boolean isNewUser, IdpCredentials idpTokens) {
    JsonObject responseJson = JsonObject.mapFrom(authResponse);

    JsonArray mfaFactorsArray = responseJson.getJsonArray(MFA_FACTORS);
    List<MfaFactorDto> mfaFactors =
        mfaFactorsArray != null ? mapToMfaFactors(mfaFactorsArray) : null;

    return new IdpConnectResponseDto(
        responseJson.getString(APP_CODE),
        responseJson.getString(APP_ACCESS_TOKEN),
        responseJson.getString(APP_REFRESH_TOKEN),
        responseJson.getString(APP_ID_TOKEN),
        responseJson.getString(APP_TOKEN_TYPE),
        responseJson.getInteger(APP_TOKEN_CODE_EXPIRY),
        isNewUser,
        idpTokens,
        mfaFactors);
  }

  private static List<MfaFactorDto> mapToMfaFactors(JsonArray mfaFactorsArray) {
    return mfaFactorsArray.stream()
        .map(factorObj -> (JsonObject) factorObj)
        .map(
            factorObj ->
                MfaFactorDto.builder()
                    .factor(factorObj.getString(MFA_FACTOR))
                    .isEnabled(factorObj.getBoolean(MFA_IS_ENABLED))
                    .build())
        .collect(Collectors.toList());
  }
}
