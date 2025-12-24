package com.dreamsportslabs.guardian.validation.annotation;

import com.dreamsportslabs.guardian.validation.validator.ValidPublicKeyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPublicKeyValidator.class)
public @interface ValidPublicKey {
  String message() default "Invalid public key format";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
