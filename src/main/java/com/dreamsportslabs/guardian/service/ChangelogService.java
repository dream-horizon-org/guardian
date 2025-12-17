package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.CHANGELOG_NOT_FOUND;

import com.dreamsportslabs.guardian.dao.ChangelogDao;
import com.dreamsportslabs.guardian.dao.model.ChangelogModel;
import com.dreamsportslabs.guardian.dto.response.config.ChangelogDetailResponseDto;
import com.dreamsportslabs.guardian.dto.response.config.ChangelogResponseDto;
import com.dreamsportslabs.guardian.dto.response.config.ChangelogSummaryResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
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
        .map(this::mapToDetailResponseDto);
  }

  public Single<ChangelogResponseDto> getChangelogByTenant(String tenantId, int limit, int offset) {
    return Single.zip(
        changelogDao.getChangelogByTenant(tenantId, limit, offset),
        changelogDao.countChangelogByTenant(tenantId),
        (changelogs, total) ->
            ChangelogResponseDto.builder()
                .total(total)
                .changes(changelogs.stream().map(this::mapToSummaryResponseDto).toList())
                .build());
  }

  private ChangelogDetailResponseDto mapToDetailResponseDto(ChangelogModel model) {
    return ChangelogDetailResponseDto.builder()
        .id(model.getId())
        .tenantId(model.getTenantId())
        .configType(model.getConfigType())
        .operationType(model.getOperationType())
        .changedBy(model.getChangedBy())
        .changedAt(model.getChangedAt())
        .oldValues(model.getOldValues())
        .newValues(model.getNewValues())
        .build();
  }

  private ChangelogSummaryResponseDto mapToSummaryResponseDto(ChangelogModel model) {
    return ChangelogSummaryResponseDto.builder()
        .id(model.getId())
        .configType(model.getConfigType())
        .operationType(model.getOperationType())
        .changedBy(model.getChangedBy())
        .changedAt(model.getChangedAt())
        .build();
  }
}
