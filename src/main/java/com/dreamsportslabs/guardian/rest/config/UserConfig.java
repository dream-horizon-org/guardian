package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.dao.model.UserConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateUserConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateUserConfigRequestDto;
import com.dreamsportslabs.guardian.dto.response.config.UserConfigResponseDto;
import com.dreamsportslabs.guardian.service.UserConfigService;
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
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/admin/config/user-config")
public class UserConfig {
  private final UserConfigService userConfigService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> createUserConfig(CreateUserConfigRequestDto requestDto) {
    requestDto.validate();
    return userConfigService
        .createUserConfig(requestDto)
        .ignoreElement()
        .andThen(Single.just(Response.status(Response.Status.CREATED).build()))
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getUserConfig(@HeaderParam(TENANT_ID) String tenantId) {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant-id header is required");
    }

    return userConfigService
        .getUserConfig(tenantId)
        .map(this::mapToResponseDto)
        .map(userConfig -> Response.ok(userConfig).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateUserConfig(
      @HeaderParam(TENANT_ID) String tenantId, UpdateUserConfigRequestDto requestDto) {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant-id header is required");
    }

    requestDto.validate();
    return userConfigService
        .updateUserConfig(tenantId, requestDto)
        .map(this::mapToResponseDto)
        .map(userConfig -> Response.ok(userConfig).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteUserConfig(@HeaderParam(TENANT_ID) String tenantId) {
    if (StringUtils.isBlank(tenantId)) {
      throw INVALID_REQUEST.getCustomException("tenant-id header is required");
    }

    return userConfigService
        .deleteUserConfig(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }

  private UserConfigResponseDto mapToResponseDto(UserConfigModel model) {
    return UserConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .isSslEnabled(model.getIsSslEnabled())
        .host(model.getHost())
        .port(model.getPort())
        .getUserPath(model.getGetUserPath())
        .createUserPath(model.getCreateUserPath())
        .authenticateUserPath(model.getAuthenticateUserPath())
        .addProviderPath(model.getAddProviderPath())
        .sendProviderDetails(model.getSendProviderDetails())
        .build();
  }
}

