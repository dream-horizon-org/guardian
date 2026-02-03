package com.dreamsportslabs.guardian.validation.annotation;

import com.dreamsportslabs.guardian.validation.validator.NotBlankIfPresentValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotBlankIfPresentValidator.class)
public @interface NotBlankIfPresent {
  String message() default "field cannot be blank";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
