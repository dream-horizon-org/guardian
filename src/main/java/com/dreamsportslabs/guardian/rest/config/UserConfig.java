package com.dreamsportslabs.guardian.rest.config;

import com.dreamsportslabs.guardian.dto.request.config.UpdateUserConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.UserConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.UserConfigService;
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
@Path("/v1/admin/config/user")
public class UserConfig {
  private final UserConfigService userConfigService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getUserConfig(@HeaderParam("tenant-id") String tenantId) {
    return userConfigService
        .getUserConfig(tenantId)
        .map(config -> UserConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateUserConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull UpdateUserConfigRequestDto requestDto) {
    requestDto.validate();
    return userConfigService
        .updateUserConfig(tenantId, requestDto)
        .map(config -> UserConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }
}
