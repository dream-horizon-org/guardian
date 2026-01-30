package com.dreamsportslabs.guardian.rest.config;

import com.dreamsportslabs.guardian.dto.request.config.CreateOidcConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOidcConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.OidcConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.OidcConfigService;
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
@Path("/v1/admin/config/oidc")
public class OidcConfig {
  private final OidcConfigService oidcConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createOidcConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull CreateOidcConfigRequestDto requestDto) {
    return oidcConfigService
        .createOidcConfig(tenantId, requestDto)
        .map(config -> OidcConfigResponseDto.from(tenantId, config))
        .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getOidcConfig(@HeaderParam("tenant-id") String tenantId) {
    return oidcConfigService
        .getOidcConfig(tenantId)
        .map(config -> OidcConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateOidcConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull UpdateOidcConfigRequestDto requestDto) {
    requestDto.validate();
    return oidcConfigService
        .updateOidcConfig(tenantId, requestDto)
        .map(config -> OidcConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteOidcConfig(@HeaderParam("tenant-id") String tenantId) {
    return oidcConfigService
        .deleteOidcConfig(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
