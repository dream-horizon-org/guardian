package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dao.model.SmsConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateSmsConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateSmsConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.SmsConfigResponseDto;
import com.dreamsportslabs.guardian.service.SmsConfigService;
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
@Path("/v1/admin/config/sms-config")
public class SmsConfig {
  private final SmsConfigService smsConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createSmsConfig(CreateSmsConfigRequestDto requestDto) {
    requestDto.validate();
    return smsConfigService
        .createSmsConfig(requestDto)
        .ignoreElement()
        .andThen(Single.just(Response.status(Response.Status.CREATED).build()))
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getSmsConfig(@HeaderParam(TENANT_ID) String tenantId) {
    return smsConfigService
        .getSmsConfig(tenantId)
        .map(this::mapToResponseDto)
        .map(smsConfig -> Response.ok(smsConfig).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateSmsConfig(
      @HeaderParam(TENANT_ID) String tenantId, UpdateSmsConfigRequestDto requestDto) {
    requestDto.validate();
    return smsConfigService
        .updateSmsConfig(tenantId, requestDto)
        .map(this::mapToResponseDto)
        .map(smsConfig -> Response.ok(smsConfig).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteSmsConfig(@HeaderParam(TENANT_ID) String tenantId) {
    return smsConfigService
        .deleteSmsConfig(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }

  private SmsConfigResponseDto mapToResponseDto(SmsConfigModel model) {
    return SmsConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .isSslEnabled(model.getIsSslEnabled())
        .host(model.getHost())
        .port(model.getPort())
        .sendSmsPath(model.getSendSmsPath())
        .templateName(model.getTemplateName())
        .templateParams(model.getTemplateParams())
        .build();
  }
}
