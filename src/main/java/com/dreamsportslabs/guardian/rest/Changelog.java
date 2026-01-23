package com.dreamsportslabs.guardian.rest;

import com.dreamsportslabs.guardian.dto.request.GetChangelogRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/v1/admin/config/changelog")
public class Changelog {
  private final ChangelogService changelogService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getChangelog(
      @Valid @BeanParam GetChangelogRequestDto requestDto) {
    return changelogService
        .getChangelogByTenant(
            requestDto.getTenantId(), requestDto.getLimit(), requestDto.getOffset())
        .map(changelog -> Response.ok(changelog).build())
        .toCompletionStage();
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> getChangelogById(@PathParam("id") Long id) {
    return changelogService
        .getChangelogById(id)
        .map(changelog -> Response.ok(changelog).build())
        .toCompletionStage();
  }
}
