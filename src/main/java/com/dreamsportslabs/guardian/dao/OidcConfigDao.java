package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.dao.query.OidcConfigQuery.CREATE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.OidcConfigQuery.DELETE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.OidcConfigQuery.GET_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.OidcConfigQuery.UPDATE_OIDC_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.OidcConfigModel;
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
public class OidcConfigDao {
  private final MysqlClient mysqlClient;

  public Single<OidcConfigModel> createOidcConfig(OidcConfigModel oidcConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_OIDC_CONFIG)
        .rxExecute(buildCreateParams(oidcConfig))
        .map(result -> oidcConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    OIDC_CONFIG_ALREADY_EXISTS.getCustomException(
                        "OIDC config already exists: " + oidcConfig.getTenantId()));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<OidcConfigModel> getOidcConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_OIDC_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, OidcConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateOidcConfig(OidcConfigModel oidcConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_OIDC_CONFIG)
        .rxExecute(buildUpdateParams(oidcConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteOidcConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_OIDC_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(OidcConfigModel oidcConfig) {
    Tuple params = Tuple.tuple().addString(oidcConfig.getTenantId());
    for (Object v : buildCommonValues(oidcConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(OidcConfigModel oidcConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(oidcConfig)) {
      params.addValue(v);
    }
    params.addString(oidcConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(OidcConfigModel oidcConfig) {
    List<Object> values = new ArrayList<>();
    values.add(oidcConfig.getIssuer());
    values.add(oidcConfig.getAuthorizationEndpoint());
    values.add(oidcConfig.getTokenEndpoint());
    values.add(oidcConfig.getUserinfoEndpoint());
    values.add(oidcConfig.getRevocationEndpoint());
    values.add(oidcConfig.getJwksUri());
    values.add(oidcConfig.getGrantTypesSupported());
    values.add(oidcConfig.getResponseTypesSupported());
    values.add(oidcConfig.getSubjectTypesSupported());
    values.add(oidcConfig.getIdTokenSigningAlgValuesSupported());
    values.add(oidcConfig.getTokenEndpointAuthMethodsSupported());
    values.add(oidcConfig.getLoginPageUri());
    values.add(oidcConfig.getConsentPageUri());
    values.add(oidcConfig.getAuthorizeTtl());
    return values;
  }
}
