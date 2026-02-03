package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.CHANGELOG_COLUMN_TOTAL;
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
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ChangelogDao {
  private final MysqlClient mysqlClient;

  public Completable logConfigChange(
      SqlConnection client,
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
            .addJsonObject(oldValues)
            .addJsonObject(newValues)
            .addString(changedBy);

    return client
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
        .filter(result -> result.size() > 0)
        .switchIfEmpty(Maybe.empty())
        .map(result -> JsonUtils.rowSetToList(result, ChangelogModel.class).get(0))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<List<ChangelogModel>> getChangelogByTenant(String tenantId, int limit, int offset) {
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
            result ->
                result.size() == 0 ? 0L : result.iterator().next().getLong(CHANGELOG_COLUMN_TOTAL))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}
