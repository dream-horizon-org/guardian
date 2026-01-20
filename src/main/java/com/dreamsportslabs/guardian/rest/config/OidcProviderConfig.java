package com.dreamsportslabs.guardian.rest.config;

import com.dreamsportslabs.guardian.dto.request.config.CreateOidcProviderConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOidcProviderConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.OidcProviderConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.OidcProviderConfigService;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/admin/config/oidc-provider")
public class OidcProviderConfig {
  private final OidcProviderConfigService oidcProviderConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createOidcProviderConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull CreateOidcProviderConfigRequestDto requestDto) {
    return oidcProviderConfigService
        .createOidcProviderConfig(tenantId, requestDto)
        .map(
            config ->
                OidcProviderConfigResponseDto.from(tenantId, requestDto.getProviderName(), config))
        .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getOidcProviderConfig(
      @HeaderParam("tenant-id") String tenantId, @QueryParam("provider_name") String providerName) {
    return oidcProviderConfigService
        .getOidcProviderConfig(tenantId, providerName)
        .map(config -> OidcProviderConfigResponseDto.from(tenantId, providerName, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateOidcProviderConfig(
      @HeaderParam("tenant-id") String tenantId,
      @QueryParam("provider_name") String providerName,
      @Valid @NotNull UpdateOidcProviderConfigRequestDto requestDto) {
    requestDto.validate();
    return oidcProviderConfigService
        .updateOidcProviderConfig(tenantId, providerName, requestDto)
        .map(config -> OidcProviderConfigResponseDto.from(tenantId, providerName, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteOidcProviderConfig(
      @HeaderParam("tenant-id") String tenantId, @QueryParam("provider_name") String providerName) {
    return oidcProviderConfigService
        .deleteOidcProviderConfig(tenantId, providerName)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
