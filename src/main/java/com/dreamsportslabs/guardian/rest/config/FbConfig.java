package com.dreamsportslabs.guardian.rest.config;

import com.dreamsportslabs.guardian.dto.request.config.CreateFbConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateFbConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.FbConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.FbConfigService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/admin/config/fb")
public class FbConfig {
  private final FbConfigService fbConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createFbConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull CreateFbConfigRequestDto requestDto) {
    return fbConfigService
        .createFbConfig(tenantId, requestDto)
        .map(config -> FbConfigResponseDto.from(tenantId, config))
        .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getFbConfig(@HeaderParam("tenant-id") String tenantId) {
    return fbConfigService
        .getFbConfig(tenantId)
        .map(config -> FbConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateFbConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull UpdateFbConfigRequestDto requestDto) {
    requestDto.validate();
    return fbConfigService
        .updateFbConfig(tenantId, requestDto)
        .map(config -> FbConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteFbConfig(@HeaderParam("tenant-id") String tenantId) {
    return fbConfigService
        .deleteFbConfig(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
