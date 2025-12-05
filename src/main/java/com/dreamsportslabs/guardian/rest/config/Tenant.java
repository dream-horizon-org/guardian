package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.dao.model.TenantModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateTenantRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTenantRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.TenantResponseDto;
import com.dreamsportslabs.guardian.service.TenantService;
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
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/admin/config/tenant")
public class Tenant {
  private final TenantService tenantService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createTenant(CreateTenantRequestDto requestDto) {
    requestDto.validate();
    return tenantService
        .createTenant(requestDto)
        .ignoreElement()
        .andThen(Single.just(Response.status(Response.Status.CREATED).build()))
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getTenant(@HeaderParam(TENANT_ID) String tenantId) {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant-id header is required");
    }

    return tenantService
        .getTenant(tenantId)
        .map(this::mapToResponseDto)
        .map(tenant -> Response.ok(tenant).build())
        .toCompletionStage();
  }

  @GET
  @Path("/name")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getTenantByName(@HeaderParam("tenant-name") String name) {
    if (StringUtils.isBlank(name)) {
      throw INVALID_REQUEST.getCustomException("tenant-name header is required");
    }

    return tenantService
        .getTenantByName(name)
        .map(this::mapToResponseDto)
        .map(tenant -> Response.ok(tenant).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateTenant(
      @HeaderParam(TENANT_ID) String tenantId, UpdateTenantRequestDto requestDto) {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant-id header is required");
    }

    requestDto.validate();
    return tenantService
        .updateTenant(tenantId, requestDto)
        .map(this::mapToResponseDto)
        .map(tenant -> Response.ok(tenant).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteTenant(@HeaderParam(TENANT_ID) String tenantId) {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant-id header is required");
    }

    return tenantService
        .deleteTenant(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }

  private TenantResponseDto mapToResponseDto(TenantModel model) {
    return TenantResponseDto.builder().tenantId(model.getId()).name(model.getName()).build();
  }
}
