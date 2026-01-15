package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public final class DtoValidationUtil {

  private DtoValidationUtil() {}

  private static RuntimeException invalidRequestException(String message) {
    return INVALID_REQUEST.getCustomException(message);
  }

  public static <T> void requireNonNull(T value, String fieldName) {
    if (value == null) {
      throw invalidRequestException(String.format("%s cannot be null", fieldName));
    }
  }

  public static void requireRequestBody(Object body) {
    if (body == null) {
      throw invalidRequestException("request body is required");
    }
  }

  public static void requireAtLeastOneField(Object... fields) {
    if (Stream.of(fields).allMatch(Objects::isNull)) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }

  public static void validateString(
      String value, String fieldName, int maxLength, boolean required) {
    if (required) {
      requireNonNull(value, fieldName);
    }
    if (StringUtils.isNotBlank(value) && value.length() > maxLength) {
      throw invalidRequestException(
          String.format("%s cannot exceed %d characters", fieldName, maxLength));
    }
    if (value != null && StringUtils.isBlank(value)) {
      throw invalidRequestException(String.format("%s cannot be blank", fieldName));
    }
  }

  public static void validateInteger(
      Integer value, String fieldName, int minValue, boolean required) {
    if (required) {
      requireNonNull(value, fieldName);
    }
    if (value != null && value < minValue) {
      throw invalidRequestException(
          String.format("%s must be greater than or equal to %d", fieldName, minValue));
    }
  }

  public static void validateIntegerRange(
      Integer value, String fieldName, int minValue, int maxValue) {
    if (value == null) {
      return;
    }
    if (value < minValue || value > maxValue) {
      throw invalidRequestException(
          String.format("%s must be between %d and %d", fieldName, minValue, maxValue));
    }
  }

  public static <T extends Enum<T>> void validateEnum(T value, String fieldName, boolean required) {
    if (required && value == null) {
      throw invalidRequestException(String.format("%s cannot be blank", fieldName));
    }
  }
}
