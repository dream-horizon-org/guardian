package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.constant.Constants.USER_IDENTIFIER_HEADER_REQUIRED;

import com.dreamsportslabs.guardian.dto.request.config.CreatePasswordPinBlockConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdatePasswordPinBlockConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.PasswordPinBlockConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.PasswordPinBlockConfigService;
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
@Path("/v1/admin/config/password-pin-block")
public class PasswordPinBlockConfig {
  private final PasswordPinBlockConfigService passwordPinBlockConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createPasswordPinBlockConfig(
      @HeaderParam("tenant-id") String tenantId,
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier,
      @Valid @NotNull CreatePasswordPinBlockConfigRequestDto requestDto) {
    return passwordPinBlockConfigService
        .createConfig(tenantId, requestDto, userIdentifier)
        .map(config -> PasswordPinBlockConfigResponseDto.from(tenantId, config))
        .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getPasswordPinBlockConfig(
      @HeaderParam("tenant-id") String tenantId) {
    return passwordPinBlockConfigService
        .getPasswordPinBlockConfig(tenantId)
        .map(config -> PasswordPinBlockConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updatePasswordPinBlockConfig(
      @HeaderParam("tenant-id") String tenantId,
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier,
      @Valid @NotNull UpdatePasswordPinBlockConfigRequestDto requestDto) {
    requestDto.validate();
    return passwordPinBlockConfigService
        .updatePasswordPinBlockConfig(tenantId, requestDto, userIdentifier)
        .map(config -> PasswordPinBlockConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deletePasswordPinBlockConfig(
      @HeaderParam("tenant-id") String tenantId,
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier) {
    return passwordPinBlockConfigService
        .deletePasswordPinBlockConfig(tenantId, userIdentifier)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
