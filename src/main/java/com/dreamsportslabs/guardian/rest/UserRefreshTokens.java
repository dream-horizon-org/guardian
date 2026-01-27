package com.dreamsportslabs.guardian.rest;

import static com.dreamsportslabs.guardian.constant.Constants.APPLICATION_JWT;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;
import static com.dreamsportslabs.guardian.utils.Utils.getAccessTokenFromAuthHeader;

import com.dreamsportslabs.guardian.dto.request.GetUserRefreshTokensRequestDto;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.google.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/v1/user/refreshTokens")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class UserRefreshTokens {
  private final AuthorizationService authorizationService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces({MediaType.APPLICATION_JSON, APPLICATION_JWT})
  public CompletionStage<Response> getActiveRefreshTokens(
      @Context HttpHeaders headers,
      @HeaderParam(TENANT_ID) String tenantId,
      @Valid GetUserRefreshTokensRequestDto requestDto) {

    String accessToken =
        getAccessTokenFromAuthHeader(headers.getHeaderString(HttpHeaders.AUTHORIZATION));

    return authorizationService
        .getActiveRefreshTokensForUser(tenantId, accessToken, requestDto.getClientId())
        .map(tokens -> Response.ok(tokens).build())
        .toCompletionStage();
  }
}
