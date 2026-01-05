package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.utils.Utils.validateTenantIdHeader;

import com.dreamsportslabs.guardian.dto.request.config.CreateOidcConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOidcConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.OidcConfigResponseDto;
import com.dreamsportslabs.guardian.service.OidcConfigService;
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
@Path("/v1/admin/config/oidc-config")
public class OidcConfig {
  private final OidcConfigService oidcConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createOidcConfig(
      @HeaderParam("tenant-id") String tenantId, CreateOidcConfigRequestDto requestDto) {
    requestDto.validate();
    validateTenantIdHeader(tenantId, requestDto.getTenantId());
    return oidcConfigService
        .createOidcConfig(requestDto)
        .map(OidcConfigResponseDto::from)
        .map(config -> Response.status(Response.Status.CREATED).entity(config).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getOidcConfig(@HeaderParam("tenant-id") String tenantId) {
    return oidcConfigService
        .getOidcConfig(tenantId)
        .map(OidcConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateOidcConfig(
      @HeaderParam("tenant-id") String tenantId, UpdateOidcConfigRequestDto requestDto) {
    requestDto.validate();
    return oidcConfigService
        .updateOidcConfig(tenantId, requestDto)
        .map(OidcConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
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
