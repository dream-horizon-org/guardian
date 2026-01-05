package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.NO_FIELDS_TO_UPDATE;

import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public final class DtoValidationUtil {

  private DtoValidationUtil() {}

  public static void requireAtLeastOneField(Object... fields) {
    if (Stream.of(fields).noneMatch(Objects::nonNull)) {
      throw NO_FIELDS_TO_UPDATE.getException();
    }
  }

  public static void validateString(
      String value, String fieldName, int maxLength, boolean requireNonBlank) {
    if (value == null) {
      return;
    }
    if (requireNonBlank && StringUtils.isBlank(value)) {
      throw INVALID_REQUEST.getCustomException(fieldName + " cannot be blank");
    }
    if (value.length() > maxLength) {
      throw INVALID_REQUEST.getCustomException(
          fieldName + " cannot exceed " + maxLength + " characters");
    }
  }

  public static void validateRequiredString(String value, String fieldName, int maxLength) {
    if (StringUtils.isBlank(value)) {
      throw INVALID_REQUEST.getCustomException(fieldName + " cannot be blank");
    }
    if (value.length() > maxLength) {
      throw INVALID_REQUEST.getCustomException(
          fieldName + " cannot exceed " + maxLength + " characters");
    }
  }

  public static void validateInteger(Integer value, String fieldName, int minValue) {
    if (value == null) {
      return;
    }
    if (value < minValue) {
      throw INVALID_REQUEST.getCustomException(
          fieldName + " must be greater than " + (minValue - 1));
    }
  }

  public static void validateIntegerRange(
      Integer value, String fieldName, int minValue, int maxValue) {
    if (value == null) {
      return;
    }
    if (value < minValue || value > maxValue) {
      throw INVALID_REQUEST.getCustomException(
          fieldName + " must be between " + minValue + " and " + maxValue);
    }
  }

  public static void validateRequiredInteger(Integer value, String fieldName, int minValue) {
    if (value == null) {
      throw INVALID_REQUEST.getCustomException(fieldName + " cannot be null");
    }
    if (value < minValue) {
      throw INVALID_REQUEST.getCustomException(
          fieldName + " must be greater than or equal to " + minValue);
    }
  }

  public static <T> void validateRequired(T value, String fieldName) {
    if (value == null) {
      throw INVALID_REQUEST.getCustomException(fieldName + " cannot be null");
    }
  }
}
