package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_PUBLIC_KEY;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BiometricCryptoUtils {
  private static final String EC_ALGORITHM = "EC";
  private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";

  private static final KeyFactory EC_KEY_FACTORY;

  static {
    try {
      EC_KEY_FACTORY = KeyFactory.getInstance(EC_ALGORITHM);
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new RuntimeException("EC algorithm not available", e);
    }
  }

  public static PublicKey convertPemPublicKeyToPublicKey(String pemPublicKey) {
    try {
      String publicKeyContent =
          pemPublicKey
              .replace("-----BEGIN PUBLIC KEY-----", "")
              .replace("-----END PUBLIC KEY-----", "")
              .replaceAll("\\s", "");

      byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);

      java.security.spec.X509EncodedKeySpec keySpec =
          new java.security.spec.X509EncodedKeySpec(keyBytes);
      return EC_KEY_FACTORY.generatePublic(keySpec);
    } catch (Exception e) {
      log.error("Failed to convert PEM public key", e);
      throw INVALID_PUBLIC_KEY.getCustomException(
          "Failed to convert PEM public key: " + e.getMessage());
    }
  }

  public static boolean verifySignature(PublicKey publicKey, byte[] data, String signature) {
    try {
      byte[] signatureBytes = Base64.getDecoder().decode(signature);

      Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
      sig.initVerify(publicKey);
      sig.update(data);
      return sig.verify(signatureBytes);
    } catch (Exception e) {
      log.error("Signature verification failed", e);
      return false;
    }
  }
}
