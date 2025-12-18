package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.utils.Utils.validateTenantIdHeader;

import com.dreamsportslabs.guardian.dto.request.config.CreateOtpConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOtpConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.OtpConfigResponseDto;
import com.dreamsportslabs.guardian.service.OtpConfigService;
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
@Path("/v1/admin/config/otp-config")
public class OtpConfig {
  private final OtpConfigService otpConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createOtpConfig(
      @HeaderParam("tenant-id") String tenantId, CreateOtpConfigRequestDto requestDto) {
    requestDto.validate();
    validateTenantIdHeader(tenantId, requestDto.getTenantId());
    return otpConfigService
        .createOtpConfig(requestDto)
        .map(OtpConfigResponseDto::from)
        .map(config -> Response.status(Response.Status.CREATED).entity(config).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getOtpConfig(@HeaderParam("tenant-id") String tenantId) {
    return otpConfigService
        .getOtpConfig(tenantId)
        .map(OtpConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateOtpConfig(
      @HeaderParam("tenant-id") String tenantId, UpdateOtpConfigRequestDto requestDto) {
    requestDto.validate();
    return otpConfigService
        .updateOtpConfig(tenantId, requestDto)
        .map(OtpConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteOtpConfig(@HeaderParam("tenant-id") String tenantId) {
    return otpConfigService
        .deleteOtpConfig(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
