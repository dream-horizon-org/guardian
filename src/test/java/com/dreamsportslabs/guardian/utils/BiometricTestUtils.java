package com.dreamsportslabs.guardian.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for biometric testing operations including EC key generation and signature creation
 */
public class BiometricTestUtils {

  private static final String EC_ALGORITHM = "EC";
  private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
  private static final String CURVE_NAME = "secp256r1"; // P-256, same as ES256

  /**
   * Generates an EC key pair for testing biometric authentication
   *
   * @return Map containing "publicKey" (PEM string), "privateKey" (PrivateKey object), and
   *     "publicKeyObject" (PublicKey object)
   * @throws Exception if key generation fails
   */
  public static Map<String, Object> generateECKeyPair() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(EC_ALGORITHM);
    ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE_NAME);
    keyPairGenerator.initialize(ecSpec, new SecureRandom());

    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    PublicKey publicKey = keyPair.getPublic();
    PrivateKey privateKey = keyPair.getPrivate();

    String publicKeyPem = convertPublicKeyToPem(publicKey);

    Map<String, Object> result = new HashMap<>();
    result.put("publicKey", publicKeyPem);
    result.put("privateKey", privateKey);
    result.put("publicKeyObject", publicKey);
    return result;
  }

  /**
   * Signs data using EC private key with SHA256withECDSA algorithm
   *
   * @param privateKey EC private key
   * @param data Data to sign
   * @return Base64-encoded DER signature
   * @throws Exception if signing fails
   */
  public static String signData(PrivateKey privateKey, byte[] data) throws Exception {
    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    signature.initSign(privateKey);
    signature.update(data);
    byte[] signatureBytes = signature.sign();
    return Base64.getEncoder().encodeToString(signatureBytes);
  }

  /**
   * Signs a Base64-encoded challenge string
   *
   * @param privateKey EC private key
   * @param challengeBase64 Base64-encoded challenge string
   * @return Base64-encoded DER signature
   * @throws Exception if signing fails
   */
  public static String signChallenge(PrivateKey privateKey, String challengeBase64)
      throws Exception {
    byte[] challengeBytes = Base64.getDecoder().decode(challengeBase64);
    return signData(privateKey, challengeBytes);
  }

  /**
   * Converts an EC public key to PEM format
   *
   * @param publicKey EC public key
   * @return PEM-encoded public key string
   */
  public static String convertPublicKeyToPem(PublicKey publicKey) {
    byte[] encoded = publicKey.getEncoded();
    String base64Encoded = Base64.getEncoder().encodeToString(encoded);

    // Format as PEM with proper line breaks (64 characters per line)
    StringBuilder pem = new StringBuilder();
    pem.append("-----BEGIN PUBLIC KEY-----\n");

    int index = 0;
    while (index < base64Encoded.length()) {
      int endIndex = Math.min(index + 64, base64Encoded.length());
      pem.append(base64Encoded, index, endIndex);
      pem.append("\n");
      index = endIndex;
    }

    pem.append("-----END PUBLIC KEY-----");
    return pem.toString();
  }

  /**
   * Verifies a signature against data using a public key
   *
   * @param publicKey EC public key
   * @param data Original data that was signed
   * @param signatureBase64 Base64-encoded signature
   * @return true if signature is valid, false otherwise
   * @throws Exception if verification fails
   */
  public static boolean verifySignature(PublicKey publicKey, byte[] data, String signatureBase64)
      throws Exception {
    byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    signature.initVerify(publicKey);
    signature.update(data);
    return signature.verify(signatureBytes);
  }

  /**
   * Creates an invalid PEM public key string (for testing error cases)
   *
   * @return Invalid PEM string
   */
  public static String createInvalidPublicKeyPem() {
    return "-----BEGIN PUBLIC KEY-----\nInvalidBase64Content!!!\n-----END PUBLIC KEY-----";
  }

  /**
   * Creates a PEM public key with wrong algorithm (RSA instead of EC) for testing
   *
   * @return RSA PEM public key
   * @throws Exception if key generation fails
   */
  public static String generateRSAPublicKeyPem() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    return convertPublicKeyToPem(keyPair.getPublic());
  }

  /**
   * Generates a random credential ID for testing
   *
   * @return Random credential ID string
   */
  public static String generateCredentialId() {
    byte[] randomBytes = new byte[32];
    new SecureRandom().nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }

  /**
   * Creates an invalid Base64 string for testing signature validation
   *
   * @return Invalid Base64 string
   */
  public static String createInvalidBase64String() {
    return "This is not valid Base64!!!";
  }

  /**
   * Signs wrong data to create an invalid signature (for testing signature verification failure)
   *
   * @param privateKey EC private key
   * @param wrongData Wrong data to sign
   * @return Base64-encoded signature that won't match the actual challenge
   * @throws Exception if signing fails
   */
  public static String createInvalidSignature(PrivateKey privateKey, String wrongData)
      throws Exception {
    return signData(privateKey, wrongData.getBytes());
  }
}
