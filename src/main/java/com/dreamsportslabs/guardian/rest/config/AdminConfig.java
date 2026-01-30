package com.dreamsportslabs.guardian.rest.config;

import com.dreamsportslabs.guardian.dto.request.config.CreateAdminConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateAdminConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.AdminConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.AdminConfigService;
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
@Path("/v1/admin/config/admin")
public class AdminConfig {
  private final AdminConfigService adminConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createAdminConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull CreateAdminConfigRequestDto requestDto) {
    return adminConfigService
        .createAdminConfig(tenantId, requestDto)
        .map(config -> AdminConfigResponseDto.from(tenantId, config))
        .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getAdminConfig(@HeaderParam("tenant-id") String tenantId) {
    return adminConfigService
        .getAdminConfig(tenantId)
        .map(config -> AdminConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateAdminConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull UpdateAdminConfigRequestDto requestDto) {
    requestDto.validate();
    return adminConfigService
        .updateAdminConfig(tenantId, requestDto)
        .map(config -> AdminConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteAdminConfig(@HeaderParam("tenant-id") String tenantId) {
    return adminConfigService
        .deleteAdminConfig(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
