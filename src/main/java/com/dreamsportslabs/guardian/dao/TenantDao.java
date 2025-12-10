package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.dao.query.TenantQuery.CREATE_TENANT;
import static com.dreamsportslabs.guardian.dao.query.TenantQuery.CREATE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.TenantQuery.CREATE_USER_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.TenantQuery.DELETE_TENANT;
import static com.dreamsportslabs.guardian.dao.query.TenantQuery.GET_TENANT;
import static com.dreamsportslabs.guardian.dao.query.TenantQuery.GET_TENANT_BY_NAME;
import static com.dreamsportslabs.guardian.dao.query.TenantQuery.UPDATE_TENANT;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_NAME_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.TenantModel;
import com.dreamsportslabs.guardian.dao.model.TokenConfigModel;
import com.dreamsportslabs.guardian.dao.model.UserConfigModel;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TenantDao {
  private final MysqlClient mysqlClient;

  public Single<TenantModel> createTenant(TenantModel tenant) {
    Tuple params = Tuple.tuple().addString(tenant.getId()).addString(tenant.getName());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_TENANT)
        .rxExecute(params)
        .map(result -> tenant)
        .onErrorResumeNext(
            err ->
                handleDuplicateTenantError(
                    err, tenant.getName(), tenant.getId(), TENANT_ALREADY_EXISTS));
  }

  public Maybe<TenantModel> getTenant(String tenantId) {
    return executeTenantQuery(GET_TENANT, Tuple.of(tenantId));
  }

  public Maybe<TenantModel> getTenantByName(String name) {
    return executeTenantQuery(GET_TENANT_BY_NAME, Tuple.of(name));
  }

  private Maybe<TenantModel> executeTenantQuery(String query, Tuple params) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(query)
        .rxExecute(params)
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, TenantModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateTenant(String tenantId, String name) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_TENANT)
        .rxExecute(Tuple.of(name, tenantId))
        .ignoreElement()
        .onErrorResumeNext(
            err ->
                handleDuplicateTenantError(err, name, tenantId, TENANT_NAME_ALREADY_EXISTS)
                    .ignoreElement());
  }

  public Single<Boolean> deleteTenant(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_TENANT)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable createDefaultUserConfig(UserConfigModel userConfig) {
    Tuple params =
        Tuple.tuple()
            .addString(userConfig.getTenantId())
            .addBoolean(userConfig.getIsSslEnabled())
            .addString(userConfig.getHost())
            .addInteger(userConfig.getPort())
            .addString(userConfig.getGetUserPath())
            .addString(userConfig.getCreateUserPath())
            .addString(userConfig.getAuthenticateUserPath())
            .addString(userConfig.getAddProviderPath())
            .addBoolean(userConfig.getSendProviderDetails());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_USER_CONFIG)
        .rxExecute(params)
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable createDefaultTokenConfig(TokenConfigModel tokenConfig) {
    Tuple params =
        Tuple.tuple()
            .addString(tokenConfig.getTenantId())
            .addString(tokenConfig.getAlgorithm())
            .addString(tokenConfig.getIssuer())
            .addString(tokenConfig.getRsaKeys())
            .addInteger(tokenConfig.getAccessTokenExpiry())
            .addInteger(tokenConfig.getRefreshTokenExpiry())
            .addInteger(tokenConfig.getIdTokenExpiry())
            .addString(tokenConfig.getIdTokenClaims())
            .addString(tokenConfig.getCookieSameSite())
            .addString(tokenConfig.getCookieDomain())
            .addString(tokenConfig.getCookiePath())
            .addBoolean(tokenConfig.getCookieSecure())
            .addBoolean(tokenConfig.getCookieHttpOnly())
            .addString(tokenConfig.getAccessTokenClaims());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_TOKEN_CONFIG)
        .rxExecute(params)
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private <T> Single<T> handleDuplicateTenantError(
      Throwable err, String name, String tenantId, ErrorEnum defaultError) {
    if (err instanceof MySQLException mySQLException) {
      int errorCode = mySQLException.getErrorCode();
      if (errorCode == 1062) {
        String errorMessage = mySQLException.getMessage();
        if (errorMessage != null && errorMessage.contains("tenant_name")) {
          return Single.error(
              TENANT_NAME_ALREADY_EXISTS.getCustomException("Tenant name already exists: " + name));
        } else if (defaultError == TENANT_ALREADY_EXISTS) {
          return Single.error(
              TENANT_ALREADY_EXISTS.getCustomException("Tenant ID already exists: " + tenantId));
        }
      }
    }
    return Single.error(INTERNAL_SERVER_ERROR.getException(err));
  }
}
