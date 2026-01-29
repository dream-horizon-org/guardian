package com.dreamsportslabs.guardian.rest.v2;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;
import static com.dreamsportslabs.guardian.utils.Utils.getAccessTokenFromAuthHeader;

import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.AuthorizationService;
import com.google.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Path("/v2/user/refresh-tokens")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class V2UserRefreshTokens {
  private final AuthorizationService authorizationService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getActiveRefreshTokens(
      @Context HttpHeaders headers,
      @HeaderParam(TENANT_ID) String tenantId,
      @QueryParam("client_id") String clientId) {

    if (StringUtils.isBlank(clientId)) {
      throw ErrorEnum.INVALID_REQUEST.getCustomException("client_id is required");
    }

    String accessToken =
        getAccessTokenFromAuthHeader(headers.getHeaderString(HttpHeaders.AUTHORIZATION));

    return authorizationService
        .getActiveRefreshTokensForUser(tenantId, accessToken, clientId)
        .map(tokens -> Response.ok(tokens).build())
        .toCompletionStage();
  }
}
