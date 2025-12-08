package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.ChangelogQuery.COUNT_CHANGELOG_BY_TENANT;
import static com.dreamsportslabs.guardian.dao.query.ChangelogQuery.GET_CHANGELOG_BY_ID;
import static com.dreamsportslabs.guardian.dao.query.ChangelogQuery.GET_CHANGELOG_BY_TENANT;
import static com.dreamsportslabs.guardian.dao.query.ChangelogQuery.LOG_CONFIG_CHANGE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.ChangelogModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ChangelogDao {
  private final MysqlClient mysqlClient;

  public Completable logConfigChange(
      String tenantId,
      String configType,
      String operationType,
      JsonObject oldValues,
      JsonObject newValues,
      String changedBy) {
    Tuple params =
        Tuple.tuple()
            .addString(tenantId)
            .addString(configType)
            .addString(operationType)
            .addString(oldValues != null ? oldValues.encode() : null)
            .addString(newValues != null ? newValues.encode() : null)
            .addString(changedBy);

    return mysqlClient
        .getWriterPool()
        .preparedQuery(LOG_CONFIG_CHANGE)
        .rxExecute(params)
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Maybe<ChangelogModel> getChangelogById(Long id) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_CHANGELOG_BY_ID)
        .rxExecute(Tuple.of(id))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, ChangelogModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<java.util.List<ChangelogModel>> getChangelogByTenant(
      String tenantId, int limit, int offset) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_CHANGELOG_BY_TENANT)
        .rxExecute(Tuple.of(tenantId, limit, offset))
        .map(result -> JsonUtils.rowSetToList(result, ChangelogModel.class))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Long> countChangelogByTenant(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(COUNT_CHANGELOG_BY_TENANT)
        .rxExecute(Tuple.of(tenantId))
        .map(
            result -> {
              if (result.size() == 0) {
                return 0L;
              }
              Row row = result.iterator().next();
              return row.getLong("total");
            })
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}
