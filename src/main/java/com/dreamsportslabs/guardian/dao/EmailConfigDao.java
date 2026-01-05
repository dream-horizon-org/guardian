package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
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
import java.util.ArrayList;
import java.util.List;
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
                  && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
                return Single.error(
                    EMAIL_CONFIG_ALREADY_EXISTS.getCustomException(
                        "Email config already exists: " + emailConfig.getTenantId()));
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
    Tuple params = Tuple.tuple().addString(emailConfig.getTenantId());
    for (Object v : buildCommonValues(emailConfig)) {
      params.addValue(v);
    }
    return params;
  }

  private Tuple buildUpdateParams(EmailConfigModel emailConfig) {
    Tuple params = Tuple.tuple();
    for (Object v : buildCommonValues(emailConfig)) {
      params.addValue(v);
    }
    params.addString(emailConfig.getTenantId());
    return params;
  }

  private List<Object> buildCommonValues(EmailConfigModel emailConfig) {
    List<Object> values = new ArrayList<>();
    values.add(emailConfig.getIsSslEnabled());
    values.add(emailConfig.getHost());
    values.add(emailConfig.getPort());
    values.add(emailConfig.getSendEmailPath());
    values.add(emailConfig.getTemplateName());
    values.add(emailConfig.getTemplateParams());
    return values;
  }
}
