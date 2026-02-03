package com.dreamsportslabs.guardian.rest.config;

import com.dreamsportslabs.guardian.dto.request.config.CreateContactVerifyConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateContactVerifyConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.ContactVerifyConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.ContactVerifyConfigService;
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
@Path("/v1/admin/config/contact-verify")
public class ContactVerifyConfig {
  private final ContactVerifyConfigService contactVerifyConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createContactVerifyConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull CreateContactVerifyConfigRequestDto requestDto) {
    return contactVerifyConfigService
        .createContactVerifyConfig(tenantId, requestDto)
        .map(config -> ContactVerifyConfigResponseDto.from(tenantId, config))
        .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getContactVerifyConfig(
      @HeaderParam("tenant-id") String tenantId) {
    return contactVerifyConfigService
        .getContactVerifyConfig(tenantId)
        .map(config -> ContactVerifyConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateContactVerifyConfig(
      @HeaderParam("tenant-id") String tenantId,
      @Valid @NotNull UpdateContactVerifyConfigRequestDto requestDto) {
    requestDto.validate();
    return contactVerifyConfigService
        .updateContactVerifyConfig(tenantId, requestDto)
        .map(config -> ContactVerifyConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteContactVerifyConfig(
      @HeaderParam("tenant-id") String tenantId) {
    return contactVerifyConfigService
        .deleteContactVerifyConfig(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
