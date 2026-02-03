package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_TENANT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_TENANT_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.MYSQL_ERROR_CODE_DUPLICATE_ENTRY;
import static com.dreamsportslabs.guardian.constant.Constants.TENANT_NAME;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_NAME_ALREADY_EXISTS;

import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.rxjava3.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.lang3.tuple.Pair;

public class SqlUtils {
  public static Pair<String, Tuple> prepareUpdateQuery(Object object) {
    List<Object> values = new ArrayList<>();
    StringJoiner insertSetFields = new StringJoiner(", ");

    JsonObject jsonObject = JsonObject.mapFrom(object);

    for (String key : jsonObject.fieldNames()) {
      Object value = jsonObject.getValue(key);

      if (key == null) {
        continue;
      } else {
        key = convertCamelToSnake(key);
      }

      if (value == null) {
        continue;
      }

      insertSetFields.add(key + " = ?"); // MySQL uses ? placeholders
      values.add(value);
    }

    if (values.isEmpty()) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }

    return Pair.of(insertSetFields.toString(), Tuple.wrap(values));
  }

  public static String convertCamelToSnake(String camelCaseString) {
    PropertyNamingStrategies.SnakeCaseStrategy snakeCaseStrategy =
        new PropertyNamingStrategies.SnakeCaseStrategy();

    return snakeCaseStrategy.translate(camelCaseString);
  }

  public static <T> Single<T> handleMySqlError(
      Throwable error, ErrorEnum customError, String message, ErrorEnum defaultError) {

    if (error instanceof MySQLException mySqlException) {
      int errorCode = mySqlException.getErrorCode();

      if (errorCode == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
        return Single.error(customError.getCustomException(message));
      }
    }
    return Single.error(defaultError.getException(error));
  }

  public static <T> Single<T> handleTenantError(Throwable err, String name, String tenantId) {
    if (err instanceof MySQLException mySQLException
        && mySQLException.getErrorCode() == MYSQL_ERROR_CODE_DUPLICATE_ENTRY) {
      String errorMessage = mySQLException.getMessage();
      if (errorMessage != null && errorMessage.contains(TENANT_NAME)) {
        return Single.error(
            TENANT_NAME_ALREADY_EXISTS.getCustomException(
                String.format("%s: %s", DUPLICATE_ENTRY_MESSAGE_TENANT_NAME, name)));
      }
      return Single.error(
          TENANT_ALREADY_EXISTS.getCustomException(
              String.format("%s: %s", DUPLICATE_ENTRY_MESSAGE_TENANT_ID, tenantId)));
    }
    return Single.error(INTERNAL_SERVER_ERROR.getException(err));
  }
}
