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
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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
            err -> {
              if (err instanceof MySQLException mySQLException) {
                int errorCode = mySQLException.getErrorCode();
                if (errorCode == 1062) {
                  String errorMessage = mySQLException.getMessage();
                  if (errorMessage != null && errorMessage.contains("tenant_name")) {
                    return Single.error(
                        TENANT_NAME_ALREADY_EXISTS.getCustomException(
                            "Tenant name already exists: " + tenant.getName()));
                  } else {
                    return Single.error(
                        TENANT_ALREADY_EXISTS.getCustomException(
                            "Tenant ID already exists: " + tenant.getId()));
                  }
                }
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<TenantModel> getTenant(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_TENANT)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, TenantModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Maybe<TenantModel> getTenantByName(String name) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_TENANT_BY_NAME)
        .rxExecute(Tuple.of(name))
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
            err -> {
              if (err instanceof MySQLException mySQLException) {
                int errorCode = mySQLException.getErrorCode();
                if (errorCode == 1062) {
                  String errorMessage = mySQLException.getMessage();
                  if (errorMessage != null && errorMessage.contains("tenant_name")) {
                    return Completable.error(
                        TENANT_NAME_ALREADY_EXISTS.getCustomException(
                            "Tenant name already exists: " + name));
                  }
                }
              }
              return Completable.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Single<Boolean> deleteTenant(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_TENANT)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable createDefaultUserConfig(JsonObject userConfig) {
    String tenantId = userConfig.getString("tenant_id");

    Tuple params =
        Tuple.tuple()
            .addString(tenantId)
            .addBoolean(userConfig.getBoolean("is_ssl_enabled"))
            .addString(userConfig.getString("host"))
            .addInteger(userConfig.getInteger("port"))
            .addString(userConfig.getString("get_user_path"))
            .addString(userConfig.getString("create_user_path"))
            .addString(userConfig.getString("authenticate_user_path"))
            .addString(userConfig.getString("add_provider_path"))
            .addBoolean(userConfig.getBoolean("send_provider_details"));

    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_USER_CONFIG)
        .rxExecute(params)
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable createDefaultTokenConfig(JsonObject tokenConfig) {
    String tenantId = tokenConfig.getString("tenant_id");
    JsonArray rsaKeys = tokenConfig.getJsonArray("rsa_keys");
    JsonArray idTokenClaims = tokenConfig.getJsonArray("id_token_claims");
    JsonArray accessTokenClaims = tokenConfig.getJsonArray("access_token_claims");

    Tuple params =
        Tuple.tuple()
            .addString(tenantId)
            .addString(tokenConfig.getString("algorithm"))
            .addString(tokenConfig.getString("issuer"))
            .addString(rsaKeys.encode())
            .addInteger(tokenConfig.getInteger("access_token_expiry"))
            .addInteger(tokenConfig.getInteger("refresh_token_expiry"))
            .addInteger(tokenConfig.getInteger("id_token_expiry"))
            .addString(idTokenClaims.encode())
            .addString(tokenConfig.getString("cookie_same_site"))
            .addString(tokenConfig.getString("cookie_domain"))
            .addString(tokenConfig.getString("cookie_path"))
            .addBoolean(tokenConfig.getBoolean("cookie_secure"))
            .addBoolean(tokenConfig.getBoolean("cookie_http_only"))
            .addString(accessTokenClaims.encode());

    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_TOKEN_CONFIG)
        .rxExecute(params)
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }
}
