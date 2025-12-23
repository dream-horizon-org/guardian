package com.dreamsportslabs.guardian.rest.v2;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.BiometricCompleteRequestDto;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.dreamsportslabs.guardian.service.BiometricService;
import com.google.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/v2/biometric/complete")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class V2BiometricComplete {
  private final BiometricService biometricService;
  private final AuthorizationService authorizationService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> completeBiometric(
      @Context HttpHeaders headers, @Valid BiometricCompleteRequestDto requestDto) {
    String tenantId = headers.getHeaderString(TENANT_ID);
    return biometricService
        .completeBiometric(requestDto, headers.getRequestHeaders(), tenantId)
        .map(
            tokenResponse -> {
              NewCookie accessTokenCookie =
                  authorizationService.getAccessTokenCookie(
                      tokenResponse.getAccessToken(), tenantId);
              NewCookie refreshTokenCookie =
                  authorizationService.getRefreshTokenCookie(
                      tokenResponse.getRefreshToken(), tenantId);
              return Response.ok(tokenResponse)
                  .cookie(accessTokenCookie, refreshTokenCookie)
                  .build();
            })
        .toCompletionStage();
  }
}
