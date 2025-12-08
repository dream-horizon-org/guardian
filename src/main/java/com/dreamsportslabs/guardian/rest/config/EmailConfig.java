package com.dreamsportslabs.guardian.rest.config;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dao.model.EmailConfigModel;
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
  public CompletionStage<Response> createEmailConfig(CreateEmailConfigRequestDto requestDto) {
    requestDto.validate();
    return emailConfigService
        .createEmailConfig(requestDto)
        .ignoreElement()
        .andThen(Single.just(Response.status(Response.Status.CREATED).build()))
        .toCompletionStage();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getEmailConfig(@HeaderParam(TENANT_ID) String tenantId) {
    return emailConfigService
        .getEmailConfig(tenantId)
        .map(this::mapToResponseDto)
        .map(emailConfig -> Response.ok(emailConfig).build())
        .toCompletionStage();
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> updateEmailConfig(
      @HeaderParam(TENANT_ID) String tenantId, UpdateEmailConfigRequestDto requestDto) {
    requestDto.validate();
    return emailConfigService
        .updateEmailConfig(tenantId, requestDto)
        .map(this::mapToResponseDto)
        .map(emailConfig -> Response.ok(emailConfig).build())
        .toCompletionStage();
  }

  @DELETE
  public CompletionStage<Response> deleteEmailConfig(@HeaderParam(TENANT_ID) String tenantId) {
    return emailConfigService
        .deleteEmailConfig(tenantId)
        .andThen(Single.just(Response.noContent().build()))
        .toCompletionStage();
  }

  private EmailConfigResponseDto mapToResponseDto(EmailConfigModel model) {
    return EmailConfigResponseDto.builder()
        .tenantId(model.getTenantId())
        .isSslEnabled(model.getIsSslEnabled())
        .host(model.getHost())
        .port(model.getPort())
        .sendEmailPath(model.getSendEmailPath())
        .templateName(model.getTemplateName())
        .templateParams(model.getTemplateParams())
        .build();
  }
}
