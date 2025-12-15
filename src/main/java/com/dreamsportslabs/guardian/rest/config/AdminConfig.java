package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.utils.Utils.validateTenantIdHeader;

import com.dreamsportslabs.guardian.dto.request.config.CreateAdminConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateAdminConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.AdminConfigResponseDto;
import com.dreamsportslabs.guardian.service.AdminConfigService;
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
@Path("/v1/admin/config/admin-config")
public class AdminConfig {
  private final AdminConfigService adminConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createAdminConfig(
      @HeaderParam("tenant-id") String tenantId, CreateAdminConfigRequestDto requestDto) {
    requestDto.validate();
    validateTenantIdHeader(tenantId, requestDto.getTenantId());
    return adminConfigService
        .createAdminConfig(requestDto)
        .map(AdminConfigResponseDto::from)
        .map(config -> Response.status(Response.Status.CREATED).entity(config).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getAdminConfig(@HeaderParam("tenant-id") String tenantId) {
    return adminConfigService
        .getAdminConfig(tenantId)
        .map(AdminConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateAdminConfig(
      @HeaderParam("tenant-id") String tenantId, UpdateAdminConfigRequestDto requestDto) {
    requestDto.validate();
    return adminConfigService
        .updateAdminConfig(tenantId, requestDto)
        .map(AdminConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
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
