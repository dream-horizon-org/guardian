package com.dreamsportslabs.guardian.validation.validator;

import com.dreamsportslabs.guardian.validation.annotation.NotBlankIfPresent;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class NotBlankIfPresentValidator implements ConstraintValidator<NotBlankIfPresent, String> {

  private String customMessage;

  @Override
  public void initialize(NotBlankIfPresent annotation) {
    this.customMessage = annotation.message();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    if (StringUtils.isBlank(value)) {
      context.disableDefaultConstraintViolation();

      String message =
          StringUtils.isNotBlank(customMessage) ? customMessage : "field cannot be blank";

      context.buildConstraintViolationWithTemplate(message).addConstraintViolation();

      return false;
    }

    return true;
  }
}
