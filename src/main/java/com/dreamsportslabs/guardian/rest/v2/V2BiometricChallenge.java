package com.dreamsportslabs.guardian.rest.v2;

import static com.dreamsportslabs.guardian.constant.Constants.TENANT_ID;

import com.dreamsportslabs.guardian.dto.request.BiometricChallengeRequestDto;
import com.dreamsportslabs.guardian.service.BiometricService;
import com.google.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/v2/biometric/challenge")
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class V2BiometricChallenge {
  private final BiometricService biometricService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> requestChallenge(
      @Context HttpHeaders headers, @Valid BiometricChallengeRequestDto requestDto) {
    String tenantId = headers.getHeaderString(TENANT_ID);
    return biometricService
        .generateChallenge(requestDto, tenantId)
        .map(response -> Response.ok(response).build())
        .toCompletionStage();
  }
}
