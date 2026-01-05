package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.utils.Utils.validateTenantIdHeader;

import com.dreamsportslabs.guardian.dto.request.config.CreateOidcProviderConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOidcProviderConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.OidcProviderConfigResponseDto;
import com.dreamsportslabs.guardian.service.OidcProviderConfigService;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/admin/config/oidc-provider-config")
public class OidcProviderConfig {
  private final OidcProviderConfigService oidcProviderConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createOidcProviderConfig(
      @HeaderParam("tenant-id") String tenantId, CreateOidcProviderConfigRequestDto requestDto) {
    requestDto.validate();
    validateTenantIdHeader(tenantId, requestDto.getTenantId());
    return oidcProviderConfigService
        .createOidcProviderConfig(requestDto)
        .map(OidcProviderConfigResponseDto::from)
        .map(config -> Response.status(Response.Status.CREATED).entity(config).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getOidcProviderConfig(
      @HeaderParam("tenant-id") String tenantId, @QueryParam("provider_name") String providerName) {
    return oidcProviderConfigService
        .getOidcProviderConfig(tenantId, providerName)
        .map(OidcProviderConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateOidcProviderConfig(
      @HeaderParam("tenant-id") String tenantId,
      @QueryParam("provider_name") String providerName,
      UpdateOidcProviderConfigRequestDto requestDto) {
    requestDto.validate();
    return oidcProviderConfigService
        .updateOidcProviderConfig(tenantId, providerName, requestDto)
        .map(OidcProviderConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
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
