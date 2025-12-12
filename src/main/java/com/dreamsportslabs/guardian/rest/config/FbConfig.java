package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.utils.Utils.validateTenantIdHeader;

import com.dreamsportslabs.guardian.dto.request.config.CreateFbConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateFbConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.FbConfigResponseDto;
import com.dreamsportslabs.guardian.service.FbConfigService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
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
@Path("/v1/admin/config/fb-config")
public class FbConfig {
  private final FbConfigService fbConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createFbConfig(
      @HeaderParam("tenant-id") String tenantId, CreateFbConfigRequestDto requestDto) {
    requestDto.validate();
    validateTenantIdHeader(tenantId, requestDto.getTenantId());
    return fbConfigService
        .createFbConfig(requestDto)
        .map(FbConfigResponseDto::from)
        .map(config -> Response.status(Response.Status.CREATED).entity(config).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getFbConfig(@HeaderParam("tenant-id") String tenantId) {
    return fbConfigService
        .getFbConfig(tenantId)
        .map(FbConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateFbConfig(
      @HeaderParam("tenant-id") String tenantId, UpdateFbConfigRequestDto requestDto) {
    requestDto.validate();
    return fbConfigService
        .updateFbConfig(tenantId, requestDto)
        .map(FbConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
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
