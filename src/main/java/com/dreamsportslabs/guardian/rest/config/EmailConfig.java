package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.utils.Utils.validateTenantIdHeader;

import com.dreamsportslabs.guardian.dto.request.config.CreateEmailConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateEmailConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.EmailConfigResponseDto;
import com.dreamsportslabs.guardian.service.EmailConfigService;
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
@Path("/v1/admin/config/email-config")
public class EmailConfig {
  private final EmailConfigService emailConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createEmailConfig(
      @HeaderParam("tenant-id") String tenantId, CreateEmailConfigRequestDto requestDto) {
    requestDto.validate();
    validateTenantIdHeader(tenantId, requestDto.getTenantId());
    return emailConfigService
        .createEmailConfig(requestDto)
        .map(EmailConfigResponseDto::from)
        .map(config -> Response.status(Response.Status.CREATED).entity(config).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getEmailConfig(@HeaderParam("tenant-id") String tenantId) {
    return emailConfigService
        .getEmailConfig(tenantId)
        .map(EmailConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateEmailConfig(
      @HeaderParam("tenant-id") String tenantId, UpdateEmailConfigRequestDto requestDto) {
    requestDto.validate();
    return emailConfigService
        .updateEmailConfig(tenantId, requestDto)
        .map(EmailConfigResponseDto::from)
        .map(config -> Response.ok(config).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteEmailConfig(@HeaderParam("tenant-id") String tenantId) {
    return emailConfigService
        .deleteEmailConfig(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
