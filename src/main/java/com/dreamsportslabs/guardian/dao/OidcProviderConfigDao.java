package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.query.OidcProviderConfigQuery.CREATE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.OidcProviderConfigQuery.DELETE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.OidcProviderConfigQuery.GET_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.OidcProviderConfigQuery.UPDATE_OIDC_PROVIDER_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_PROVIDER_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.OidcProviderConfigModel;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OidcProviderConfigDao {
  private final MysqlClient mysqlClient;

  public Single<OidcProviderConfigModel> createOidcProviderConfig(
      OidcProviderConfigModel oidcProviderConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_OIDC_PROVIDER_CONFIG)
        .rxExecute(buildCreateParams(oidcProviderConfig))
        .map(result -> oidcProviderConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    OIDC_PROVIDER_CONFIG_ALREADY_EXISTS.getCustomException(
                        "OIDC provider config already exists: "
                            + oidcProviderConfig.getTenantId()
                            + "/"
                            + oidcProviderConfig.getProviderName()));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<OidcProviderConfigModel> getOidcProviderConfig(
      String tenantId, String providerName) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_OIDC_PROVIDER_CONFIG)
        .rxExecute(Tuple.of(tenantId, providerName))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(
                  JsonUtils.rowSetToList(result, OidcProviderConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateOidcProviderConfig(OidcProviderConfigModel oidcProviderConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_OIDC_PROVIDER_CONFIG)
        .rxExecute(buildUpdateParams(oidcProviderConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteOidcProviderConfig(String tenantId, String providerName) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_OIDC_PROVIDER_CONFIG)
        .rxExecute(Tuple.of(tenantId, providerName))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(OidcProviderConfigModel oidcProviderConfig) {
    Tuple params =
        Tuple.tuple()
            .addString(oidcProviderConfig.getTenantId())
            .addString(oidcProviderConfig.getProviderName());
    for (Object v : buildCommonValues(oidcProviderConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(OidcProviderConfigModel oidcProviderConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(oidcProviderConfig)) {
      params.addValue(v);
    }
    params.addString(oidcProviderConfig.getTenantId());
    params.addString(oidcProviderConfig.getProviderName());
    return params;
  }

  private List<Object> buildCommonValues(OidcProviderConfigModel oidcProviderConfig) {
    List<Object> values = new ArrayList<>();
    values.add(oidcProviderConfig.getIssuer());
    values.add(oidcProviderConfig.getJwksUrl());
    values.add(oidcProviderConfig.getTokenUrl());
    values.add(oidcProviderConfig.getClientId());
    values.add(oidcProviderConfig.getClientSecret());
    values.add(oidcProviderConfig.getRedirectUri());
    values.add(oidcProviderConfig.getClientAuthMethod());
    values.add(oidcProviderConfig.getIsSslEnabled());
    values.add(oidcProviderConfig.getUserIdentifier());
    values.add(oidcProviderConfig.getAudienceClaims());
    return values;
  }
}
