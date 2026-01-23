package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.BaseConfigDao;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public abstract class BaseConfigService<TModel, TCreateDto, TUpdateDto> {
  protected final ChangelogService changelogService;
  protected final MysqlClient mysqlClient;
  protected final TenantCache tenantCache;

  protected abstract BaseConfigDao<TModel> getDao();

  protected abstract String getConfigType();

  protected abstract ErrorEnum getNotFoundError();

  protected abstract TModel mapToModel(TCreateDto requestDto);

  protected abstract TModel mergeModel(TUpdateDto requestDto, TModel oldModel);

  protected abstract String getCreateErrorMessage();

  protected abstract String getUpdateErrorMessage();

  public Single<TModel> createConfig(String tenantId, TCreateDto requestDto) {
    TModel model = mapToModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                getDao()
                    .createConfig(client, tenantId, model)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    getConfigType(),
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
        .switchIfEmpty(
            Single.error(INTERNAL_SERVER_ERROR.getCustomException(getCreateErrorMessage())));
  }

  public Single<TModel> getConfig(String tenantId) {
    return getDao()
        .getConfig(tenantId)
        .switchIfEmpty(Single.error(getNotFoundError().getException()));
  }

  public Single<TModel> updateConfig(String tenantId, TUpdateDto requestDto) {
    return getDao()
        .getConfig(tenantId)
        .switchIfEmpty(Single.error(getNotFoundError().getException()))
        .flatMap(
            oldConfig -> {
              TModel updatedConfig = mergeModel(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          getDao()
                              .updateConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      getConfigType(),
                                      OPERATION_UPDATE,
                                      oldConfig,
                                      updatedConfig,
                                      tenantId))
                              .andThen(Single.just(updatedConfig))
                              .toMaybe())
                  .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
                  .switchIfEmpty(
                      Single.error(
                          INTERNAL_SERVER_ERROR.getCustomException(getUpdateErrorMessage())));
            });
  }

  public Completable deleteConfig(String tenantId) {
    return getDao()
        .getConfig(tenantId)
        .switchIfEmpty(Single.error(getNotFoundError().getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            getDao()
                                .deleteConfig(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(getNotFoundError().getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          getConfigType(),
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .toMaybe())
                    .doOnComplete(() -> tenantCache.invalidateCache(tenantId))
                    .ignoreElement());
  }
}
