package com.dreamsportslabs.guardian.rest.config;

import com.dreamsportslabs.guardian.dto.request.config.UpdateTokenConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.TokenConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.TokenConfigService;
import com.google.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@Path("/v1/admin/config/token")
public class TokenConfig {
  private final TokenConfigService tokenConfigService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getTokenConfig(@HeaderParam("tenant-id") String tenantId) {
    return tokenConfigService
        .getTokenConfig(tenantId)
        .map(config -> TokenConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateTokenConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull UpdateTokenConfigRequestDto requestDto) {
    requestDto.validate();
    return tokenConfigService
        .updateTokenConfig(tenantId, requestDto)
        .map(config -> TokenConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }
}
