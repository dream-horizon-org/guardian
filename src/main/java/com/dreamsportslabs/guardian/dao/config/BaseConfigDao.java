package com.dreamsportslabs.guardian.dao.config;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.dreamsportslabs.guardian.utils.SqlUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public abstract class BaseConfigDao<TModel> {
  protected final MysqlClient mysqlClient;

  protected abstract String getCreateQuery();

  protected abstract String getGetQuery();

  protected abstract String getUpdateQuery();

  protected abstract String getDeleteQuery();

  protected abstract Tuple buildParams(String tenantId, TModel model);

  protected abstract ErrorEnum getDuplicateEntryError();

  protected abstract String getDuplicateEntryMessageFormat();

  protected abstract Class<TModel> getModelClass();

  public Single<TModel> createConfig(SqlConnection client, String tenantId, TModel model) {
    return client
        .preparedQuery(getCreateQuery())
        .rxExecute(buildParams(tenantId, model))
        .map(result -> model)
        .onErrorResumeNext(
            err ->
                SqlUtils.handleMySqlError(
                    err,
                    getDuplicateEntryError(),
                    String.format("%s: %s", getDuplicateEntryMessageFormat(), tenantId),
                    INTERNAL_SERVER_ERROR));
  }

  public Maybe<TModel> getConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(getGetQuery())
        .rxExecute(Tuple.of(tenantId))
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, getModelClass()).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateConfig(SqlConnection client, String tenantId, TModel model) {
    return client
        .preparedQuery(getUpdateQuery())
        .rxExecute(buildParams(tenantId, model))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteConfig(SqlConnection client, String tenantId) {
    return client
        .preparedQuery(getDeleteQuery())
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}
