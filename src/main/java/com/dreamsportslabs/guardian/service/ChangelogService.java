package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.CHANGELOG_NOT_FOUND;

import com.dreamsportslabs.guardian.dao.ChangelogDao;
import com.dreamsportslabs.guardian.dto.response.ChangelogDetailResponseDto;
import com.dreamsportslabs.guardian.dto.response.ChangelogResponseDto;
import com.dreamsportslabs.guardian.dto.response.ChangelogSummaryResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ChangelogService {
  private final ChangelogDao changelogDao;

  public Single<ChangelogDetailResponseDto> getChangelogById(Long id) {
    return changelogDao
        .getChangelogById(id)
        .switchIfEmpty(Single.error(CHANGELOG_NOT_FOUND.getException()))
        .map(ChangelogDetailResponseDto::from);
  }

  public Single<ChangelogResponseDto> getChangelogByTenant(String tenantId, int limit, int offset) {
    return Single.zip(
        changelogDao.getChangelogByTenant(tenantId, limit, offset),
        changelogDao.countChangelogByTenant(tenantId),
        (changelogs, total) ->
            ChangelogResponseDto.builder()
                .total(total)
                .changes(changelogs.stream().map(ChangelogSummaryResponseDto::from).toList())
                .build());
  }

  public Completable logConfigChange(
      SqlConnection client,
      String tenantId,
      String configType,
      String operationType,
      Object oldValues,
      Object newValues,
      String changedBy) {
    JsonObject oldJson = JsonObject.mapFrom(oldValues);
    JsonObject newJson = JsonObject.mapFrom(newValues);
    return changelogDao.logConfigChange(
        client, tenantId, configType, operationType, oldJson, newJson, changedBy);
  }
}
