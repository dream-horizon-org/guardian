package com.dreamsportslabs.guardian.rest.config;

import com.dreamsportslabs.guardian.dto.request.config.CreateSmsConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateSmsConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.SmsConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.SmsConfigService;
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
@Path("/v1/admin/config/sms")
public class SmsConfig {
  private final SmsConfigService smsConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createSmsConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull CreateSmsConfigRequestDto requestDto) {
    return smsConfigService
        .createSmsConfig(tenantId, requestDto)
        .map(config -> SmsConfigResponseDto.from(tenantId, config))
        .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getSmsConfig(@HeaderParam("tenant-id") String tenantId) {
    return smsConfigService
        .getSmsConfig(tenantId)
        .map(config -> SmsConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateSmsConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull UpdateSmsConfigRequestDto requestDto) {
    requestDto.validate();
    return smsConfigService
        .updateSmsConfig(tenantId, requestDto)
        .map(config -> SmsConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteSmsConfig(@HeaderParam("tenant-id") String tenantId) {
    return smsConfigService
        .deleteSmsConfig(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
