package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.constant.Constants.USER_IDENTIFIER_HEADER_REQUIRED;

import com.dreamsportslabs.guardian.dto.request.config.CreateGuestConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateGuestConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.GuestConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.GuestConfigService;
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
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/admin/config/guest")
public class GuestConfig {
  private final GuestConfigService guestConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createGuestConfig(
      @HeaderParam("tenant-id") String tenantId,
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier,
      @Valid @NotNull CreateGuestConfigRequestDto requestDto) {
    return guestConfigService
        .createGuestConfig(tenantId, requestDto, userIdentifier)
        .map(config -> GuestConfigResponseDto.from(tenantId, config))
        .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getGuestConfig(@HeaderParam("tenant-id") String tenantId) {
    return guestConfigService
        .getGuestConfig(tenantId)
        .map(config -> GuestConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateGuestConfig(
      @HeaderParam("tenant-id") String tenantId,
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier,
      @Valid @NotNull UpdateGuestConfigRequestDto requestDto) {
    requestDto.validate();
    return guestConfigService
        .updateGuestConfig(tenantId, requestDto, userIdentifier)
        .map(config -> GuestConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteGuestConfig(
      @HeaderParam("tenant-id") String tenantId,
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier) {
    return guestConfigService
        .deleteGuestConfig(tenantId, userIdentifier)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
