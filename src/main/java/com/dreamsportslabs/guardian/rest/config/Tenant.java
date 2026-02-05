package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.constant.Constants.USER_IDENTIFIER_HEADER_REQUIRED;

import com.dreamsportslabs.guardian.dto.request.config.CreateTenantRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTenantRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.TenantResponseDto;
import com.dreamsportslabs.guardian.service.config.TenantService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/admin/config/tenant")
public class Tenant {
  private final TenantService tenantService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createTenant(
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier,
      @Valid @NotNull CreateTenantRequestDto requestDto) {
    return tenantService
        .createTenant(requestDto, userIdentifier)
        .map(TenantResponseDto::from)
        .map(tenant -> Response.status(Response.Status.CREATED).entity(tenant).build())
        .toCompletionStage();
  }

  @GET
  @Path("/{tenantId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getTenant(@PathParam("tenantId") String tenantId) {
    return tenantService
        .getTenant(tenantId)
        .map(TenantResponseDto::from)
        .map(tenant -> Response.ok(tenant).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getTenantByName(@QueryParam("name") @NotBlank String name) {
    return tenantService
        .getTenantByName(name)
        .map(TenantResponseDto::from)
        .map(tenant -> Response.ok(tenant).build())
        .toCompletionStage();
  }

  @PATCH
  @Path("/{tenantId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateTenant(
      @PathParam("tenantId") String tenantId,
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier,
      @Valid @NotNull UpdateTenantRequestDto requestDto) {
    return tenantService
        .updateTenant(tenantId, requestDto, userIdentifier)
        .map(TenantResponseDto::from)
        .map(tenant -> Response.ok(tenant).build())
        .toCompletionStage();
  }

  @DELETE
  @Path("/{tenantId}")
  public CompletionStage<Response> deleteTenant(
      @PathParam("tenantId") String tenantId,
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier) {
    return tenantService
        .deleteTenant(tenantId, userIdentifier)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
