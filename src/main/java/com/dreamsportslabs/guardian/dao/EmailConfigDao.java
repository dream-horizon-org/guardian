package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.dao.query.EmailConfigQuery.CREATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.EmailConfigQuery.DELETE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.EmailConfigQuery.GET_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.dao.query.EmailConfigQuery.UPDATE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.EmailConfigModel;
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
public class EmailConfigDao {
  private final MysqlClient mysqlClient;

  public Single<EmailConfigModel> createEmailConfig(EmailConfigModel emailConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(CREATE_EMAIL_CONFIG)
        .rxExecute(buildCreateParams(emailConfig))
        .map(result -> emailConfig)
        .onErrorResumeNext(
            err -> {
              if (err instanceof MySQLException mySQLException
                  && mySQLException.getErrorCode() == 1062) {
                return Single.error(
                    EMAIL_CONFIG_ALREADY_EXISTS.getCustomException("Email config already exists: " + emailConfig.getTenantId()));
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public Maybe<EmailConfigModel> getEmailConfig(String tenantId) {
    return mysqlClient
        .getReaderPool()
        .preparedQuery(GET_EMAIL_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .flatMapMaybe(
            result -> {
              if (result.size() == 0) {
                return Maybe.empty();
              }
              return Maybe.just(JsonUtils.rowSetToList(result, EmailConfigModel.class).get(0));
            })
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Completable updateEmailConfig(EmailConfigModel emailConfig) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(UPDATE_EMAIL_CONFIG)
        .rxExecute(buildUpdateParams(emailConfig))
        .ignoreElement()
        .onErrorResumeNext(err -> Completable.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  public Single<Boolean> deleteEmailConfig(String tenantId) {
    return mysqlClient
        .getWriterPool()
        .preparedQuery(DELETE_EMAIL_CONFIG)
        .rxExecute(Tuple.of(tenantId))
        .map(result -> result.rowCount() > 0)
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)));
  }

  private Tuple buildCreateParams(EmailConfigModel emailConfig) {
    return Tuple.tuple()
        .addString(emailConfig.getTenantId())
        .addBoolean(
            emailConfig.getIsSslEnabled() != null
                ? emailConfig.getIsSslEnabled()
                : DEFAULT_IS_SSL_ENABLED)
        .addString(emailConfig.getHost())
        .addInteger(emailConfig.getPort())
        .addString(emailConfig.getSendEmailPath())
        .addString(emailConfig.getTemplateName())
        .addString(emailConfig.getTemplateParams());
  }

  private Tuple buildUpdateParams(EmailConfigModel emailConfig) {
    return Tuple.tuple()
        .addBoolean(
            emailConfig.getIsSslEnabled() != null
                ? emailConfig.getIsSslEnabled()
                : DEFAULT_IS_SSL_ENABLED)
        .addString(emailConfig.getHost())
        .addInteger(emailConfig.getPort())
        .addString(emailConfig.getSendEmailPath())
        .addString(emailConfig.getTemplateName())
        .addString(emailConfig.getTemplateParams())
        .addString(emailConfig.getTenantId());
  }
}
