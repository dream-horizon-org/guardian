package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.constant.Constants.USER_IDENTIFIER_HEADER_REQUIRED;

import com.dreamsportslabs.guardian.dto.request.config.CreateAuthCodeConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateAuthCodeConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.AuthCodeConfigResponseDto;
import com.dreamsportslabs.guardian.service.config.AuthCodeConfigService;
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
@Path("/v1/admin/config/auth-code")
public class AuthCodeConfig {
  private final AuthCodeConfigService authCodeConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createAuthCodeConfig(
      @HeaderParam("tenant-id") String tenantId,
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier,
      @Valid @NotNull CreateAuthCodeConfigRequestDto requestDto) {
    return authCodeConfigService
        .createAuthCodeConfig(tenantId, requestDto, userIdentifier)
        .map(config -> AuthCodeConfigResponseDto.from(tenantId, config))
        .map(response -> Response.status(Response.Status.CREATED).entity(response).build())
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getAuthCodeConfig(@HeaderParam("tenant-id") String tenantId) {
    return authCodeConfigService
        .getAuthCodeConfig(tenantId)
        .map(config -> AuthCodeConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateAuthCodeConfig(
      @HeaderParam("tenant-id") String tenantId,
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier,
      @Valid @NotNull UpdateAuthCodeConfigRequestDto requestDto) {
    requestDto.validate();
    return authCodeConfigService
        .updateAuthCodeConfig(tenantId, requestDto, userIdentifier)
        .map(config -> AuthCodeConfigResponseDto.from(tenantId, config))
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteAuthCodeConfig(
      @HeaderParam("tenant-id") String tenantId,
      @HeaderParam("user-identifier") @NotBlank(message = USER_IDENTIFIER_HEADER_REQUIRED)
          String userIdentifier) {
    return authCodeConfigService
        .deleteAuthCodeConfig(tenantId, userIdentifier)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }
}
