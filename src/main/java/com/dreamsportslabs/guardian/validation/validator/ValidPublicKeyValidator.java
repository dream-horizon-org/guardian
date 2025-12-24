package com.dreamsportslabs.guardian.validation.validator;

import com.dreamsportslabs.guardian.utils.BiometricCryptoUtils;
import com.dreamsportslabs.guardian.validation.annotation.ValidPublicKey;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class ValidPublicKeyValidator implements ConstraintValidator<ValidPublicKey, String> {

  @Override
  public void initialize(ValidPublicKey constraintAnnotation) {}

  @Override
  public boolean isValid(String publicKey, ConstraintValidatorContext context) {
    if (StringUtils.isBlank(publicKey)) {
      return true;
    }

    try {
      BiometricCryptoUtils.convertPemPublicKeyToPublicKey(publicKey);
      return true;
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Invalid public key format: " + e.getMessage())
          .addConstraintViolation();
      return false;
    }
  }
}
