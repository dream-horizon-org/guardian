package com.dreamsportslabs.guardian.rest.config;

import com.dreamsportslabs.guardian.dto.request.config.CreateEmailConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateEmailConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.EmailConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.EmailConfigService;
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
@Path("/v1/admin/config/email")
public class EmailConfig {
  private final EmailConfigService emailConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createEmailConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull CreateEmailConfigRequestDto requestDto) {
    return emailConfigService
        .createEmailConfig(tenantId, requestDto)
        .map(config -> EmailConfigResponseDto.from(tenantId, config))
        .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getEmailConfig(@HeaderParam("tenant-id") String tenantId) {
    return emailConfigService
        .getEmailConfig(tenantId)
        .map(config -> EmailConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateEmailConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull UpdateEmailConfigRequestDto requestDto) {
    requestDto.validate();
    return emailConfigService
        .updateEmailConfig(tenantId, requestDto)
        .map(config -> EmailConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
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
