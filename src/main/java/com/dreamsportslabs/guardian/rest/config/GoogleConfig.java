package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.utils.Utils.validateTenantIdHeader;

import com.dreamsportslabs.guardian.dto.request.config.CreateGoogleConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateGoogleConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.GoogleConfigResponseDto;
import com.dreamsportslabs.guardian.service.GoogleConfigService;
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
@Path("/v1/admin/config/google-config")
public class GoogleConfig {
  private final GoogleConfigService googleConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createGoogleConfig(
      @HeaderParam("tenant-id") String tenantId, CreateGoogleConfigRequestDto requestDto) {
    requestDto.validate();
    validateTenantIdHeader(tenantId, requestDto.getTenantId());
    return googleConfigService
        .createGoogleConfig(requestDto)
        .map(GoogleConfigResponseDto::from)
        .map(config -> Response.status(Response.Status.CREATED).entity(config).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getGoogleConfig(@HeaderParam("tenant-id") String tenantId) {
    return googleConfigService
        .getGoogleConfig(tenantId)
        .map(GoogleConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateGoogleConfig(
      @HeaderParam("tenant-id") String tenantId, UpdateGoogleConfigRequestDto requestDto) {
    requestDto.validate();
    return googleConfigService
        .updateGoogleConfig(tenantId, requestDto)
        .map(GoogleConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteGoogleConfig(@HeaderParam("tenant-id") String tenantId) {
    return googleConfigService
        .deleteGoogleConfig(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
