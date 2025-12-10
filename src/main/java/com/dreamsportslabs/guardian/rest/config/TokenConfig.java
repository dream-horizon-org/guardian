package com.dreamsportslabs.guardian.rest.config;

import com.dreamsportslabs.guardian.dao.model.TokenConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTokenConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.TokenConfigResponseDto;
import com.dreamsportslabs.guardian.service.TokenConfigService;
import com.google.inject.Inject;
import io.vertx.core.json.JsonArray;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/admin/config/token-config")
public class TokenConfig {
  private final TokenConfigService tokenConfigService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getTokenConfig(@HeaderParam("tenant-id") String tenantId) {
    return tokenConfigService
        .getTokenConfig(tenantId)
        .map(this::mapToResponseDto)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateTokenConfig(
      @HeaderParam("tenant-id") String tenantId, UpdateTokenConfigRequestDto requestDto) {
    requestDto.validate();
    return tokenConfigService
        .updateTokenConfig(tenantId, requestDto)
        .map(this::mapToResponseDto)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  private TokenConfigResponseDto mapToResponseDto(TokenConfigModel model) {
    JsonArray rsaKeysArray = new JsonArray(model.getRsaKeys());
    JsonArray idTokenClaimsArray = new JsonArray(model.getIdTokenClaims());
    JsonArray accessTokenClaimsArray = new JsonArray(model.getAccessTokenClaims());

    return TokenConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .algorithm(model.getAlgorithm())
        .issuer(model.getIssuer())
        .rsaKeys(rsaKeysArray.getList())
        .accessTokenExpiry(model.getAccessTokenExpiry())
        .refreshTokenExpiry(model.getRefreshTokenExpiry())
        .idTokenExpiry(model.getIdTokenExpiry())
        .idTokenClaims(
            idTokenClaimsArray.stream()
                .map(item -> item instanceof String ? (String) item : item.toString())
                .toList())
        .cookieSameSite(model.getCookieSameSite())
        .cookieDomain(model.getCookieDomain())
        .cookiePath(model.getCookiePath())
        .cookieSecure(model.getCookieSecure())
        .cookieHttpOnly(model.getCookieHttpOnly())
        .accessTokenClaims(
            accessTokenClaimsArray.stream()
                .map(item -> item instanceof String ? (String) item : item.toString())
                .toList())
        .build();
  }
}
