package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.constant.Constants.AES_ALGORITHM;
import static com.dreamsportslabs.guardian.constant.Constants.AES_CBC_NO_PADDING;
import static com.dreamsportslabs.guardian.constant.Constants.BASIC_AUTHENTICATION_SCHEME;
import static com.dreamsportslabs.guardian.constant.Constants.USER_AGENT;
import static com.dreamsportslabs.guardian.constant.Constants.X_FORWARDED_FOR;
import static com.dreamsportslabs.guardian.constant.Constants.prohibitedForwardingHeaders;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;
import static com.dreamsportslabs.guardian.exception.OidcErrorEnum.INVALID_TOKEN;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dto.request.GenerateRsaKeyRequestDto;
import com.dreamsportslabs.guardian.dto.response.RsaKeyResponseDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.RsaKeyPairGeneratorService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public final class Utils {

  private Utils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  // TODO: revalidate the regex
  public static boolean validateEmail(String email) {
    return Pattern.compile("^(.+)@(\\S+)$").matcher(email).matches();
  }

  // TODO: revalidate the regex
  public static boolean validateMobileNumber(String mobile) {
    // Should have 7 to 15 numbers and may or may not include internation code with + sign
    return Pattern.compile("^\\+?[0-9]{7,15}$").matcher(mobile).matches();
  }

  public static MultiMap getForwardingHeaders(MultivaluedMap<String, String> headers) {
    MultiMap forwardingHeaders = MultiMap.caseInsensitiveMultiMap();
    for (String key : headers.keySet()) {
      if (!prohibitedForwardingHeaders.contains(key.toUpperCase())) {
        forwardingHeaders.add(key, headers.getFirst(key));
      }
    }
    return forwardingHeaders;
  }

  public static String getMd5Hash(String input) {
    return DigestUtils.md5Hex(input).toUpperCase();
  }

  public static String[] getCredentialsFromAuthHeader(String authorizationHeader) {
    try {
      String prefix = authorizationHeader.substring(0, 6);
      String token = authorizationHeader.substring(6).strip();
      if (!prefix.equals(BASIC_AUTHENTICATION_SCHEME)) {
        throw ErrorEnum.UNAUTHORIZED.getException();
      }
      String credentials = new String(Base64.getDecoder().decode(token.getBytes()));
      return credentials.split(":", 2);
    } catch (Exception e) {
      throw ErrorEnum.UNAUTHORIZED.getException();
    }
  }

  public static String generateBasicAuthHeader(String clientId, String clientSecret) {
    return BASIC_AUTHENTICATION_SCHEME
        + new String(Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes()));
  }

  public static String getRftId(String refreshToken) {
    if (refreshToken == null) {
      return null;
    }
    return getMd5Hash(refreshToken);
  }

  public static String getIpFromHeaders(MultivaluedMap<String, String> headers) {
    String xForwardedFor = headers.getFirst(X_FORWARDED_FOR);
    if (!StringUtils.isBlank(xForwardedFor)) {
      String[] ips = xForwardedFor.split(",");
      if (ips.length > 0) {
        return ips[0].trim();
      }
    }
    return null;
  }

  public static String getDeviceNameFromHeaders(MultivaluedMap<String, String> headers) {
    String userAgent = headers.getFirst(USER_AGENT);
    if (StringUtils.isBlank(userAgent)) {
      return null;
    }
    return userAgent;
  }

  public static long getCurrentTimeInSeconds() {
    return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
  }

  public static String getAccessTokenFromAuthHeader(String authorizationHeader) {
    try {
      String prefix = authorizationHeader.substring(0, 7);
      String token = authorizationHeader.substring(7).strip();
      if (!prefix.equals("Bearer ")) {
        throw UNAUTHORIZED.getCustomException("Invalid authorization header format");
      }
      return token;
    } catch (Exception e) {
      throw UNAUTHORIZED.getCustomException("Invalid authorization header");
    }
  }

  public static Map<String, Object> decodeJwtHeaders(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length < 2) {
        throw INVALID_TOKEN.getBearerAuthHeaderException("Invalid JWT format");
      }
      String headerJson =
          new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
      return new ObjectMapper().readValue(headerJson, Map.class);
    } catch (Exception e) {
      throw INVALID_TOKEN.getBearerAuthHeaderException();
    }
  }

  public static JsonObject convertKeysToSnakeCase(JsonObject input) {
    JsonObject result = new JsonObject();
    for (Map.Entry<String, Object> entry : input) {
      String snakeKey = toSnakeCase(entry.getKey());
      result.put(snakeKey, entry.getValue());
    }
    return result;
  }

  private static String toSnakeCase(String input) {

    return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").replaceAll("[-\\s]", "_").toLowerCase();
  }

  public static Map<String, Object> appendAdditionalAccessTokenClaims(
      Map<String, Object> accessTokenClaims, JsonObject userResponse, TenantConfig tenantConfig) {
    Map<String, Object> additionalAccessTokenClaims = new HashMap<>(accessTokenClaims);
    if (shouldSetAccessTokenAdditionalClaims(tenantConfig)) {
      tenantConfig.getTokenConfig().getAccessTokenClaims().stream()
          .filter(userResponse::containsKey)
          .toList()
          .forEach(claim -> additionalAccessTokenClaims.put(claim, userResponse.getValue(claim)));
    }
    return additionalAccessTokenClaims;
  }

  public static boolean shouldSetAccessTokenAdditionalClaims(TenantConfig config) {
    return config.getTokenConfig().getAccessTokenClaims() != null
        && !config.getTokenConfig().getAccessTokenClaims().isEmpty();
  }

  public static String decryptUsingAESCBCAlgo(String encryptedBase64, String secretKey) {
    try {
      byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
      byte[] zeroIv = new byte[16];

      SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
      IvParameterSpec ivSpec = new IvParameterSpec(zeroIv);

      Cipher cipher = Cipher.getInstance(AES_CBC_NO_PADDING);
      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

      byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
      byte[] decrypted = cipher.doFinal(encryptedBytes);

      return new String(decrypted, StandardCharsets.UTF_8).trim();
    } catch (Exception e) {
      throw ErrorEnum.DECRYPTION_FAILED.getException();
    }
  }

  public static void validateTenantIdHeader(String headerTenantId, String bodyTenantId) {
    if (!headerTenantId.equals(bodyTenantId)) {
      throw ErrorEnum.INVALID_REQUEST.getCustomException(
          "tenant-id header must match tenant_id in request body");
    }
  }

  public static <T> T coalesce(T newValue, T oldValue) {
    return newValue != null ? newValue : oldValue;
  }

  public static GenerateRsaKeyRequestDto buildRsaKeyRequest(int keySize, String format) {
    GenerateRsaKeyRequestDto keyRequest = new GenerateRsaKeyRequestDto();
    keyRequest.setKeySize(keySize);
    keyRequest.setFormat(format);
    return keyRequest;
  }

  public static JsonObject buildRsaKeyObject(
      RsaKeyResponseDto rsaKey,
      boolean isCurrent,
      String kidField,
      String publicKeyField,
      String privateKeyField,
      String currentField) {
    JsonObject rsaKeyObject = new JsonObject();
    rsaKeyObject.put(kidField, rsaKey.getKid());
    rsaKeyObject.put(publicKeyField, rsaKey.getPublicKey().toString());
    rsaKeyObject.put(privateKeyField, rsaKey.getPrivateKey().toString());
    if (isCurrent) {
      rsaKeyObject.put(currentField, true);
    }
    return rsaKeyObject;
  }

  public static JsonArray generateRsaKeysArray(
      RsaKeyPairGeneratorService rsaKeyPairGeneratorService,
      int defaultRsaKeyCount,
      int firstRsaKeyIndex,
      int defaultRsaKeySize,
      String formatPem,
      String kidField,
      String publicKeyField,
      String privateKeyField,
      String currentField) {
    GenerateRsaKeyRequestDto keyRequest = buildRsaKeyRequest(defaultRsaKeySize, formatPem);
    JsonArray rsaKeysArray = new JsonArray();

    for (int i = 0; i < defaultRsaKeyCount; i++) {
      RsaKeyResponseDto rsaKey = rsaKeyPairGeneratorService.generateKey(keyRequest);
      JsonObject rsaKeyObject =
          buildRsaKeyObject(
              rsaKey,
              i == firstRsaKeyIndex,
              kidField,
              publicKeyField,
              privateKeyField,
              currentField);
      rsaKeysArray.add(rsaKeyObject);
    }

    return rsaKeysArray;
  }

  public static class JsonToStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      if (node.isNull()) {
        return null;
      }
      if (node.isTextual()) {
        return node.asText();
      }
      return node.toString();
    }
  }

  public static class WhitelistedInputsDeserializer extends JsonDeserializer<List<String>> {
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      if (node.isNull()) {
        return null;
      }
      if (!node.isObject()) {
        throw new IOException("whitelisted_inputs must be a JSON object");
      }
      List<String> result = new ArrayList<>();
      node.fieldNames().forEachRemaining(result::add);
      return result;
    }
  }

  public static Map<String, Object> parseWhitelistedInputs(String whitelistedInputs) {
    if (whitelistedInputs == null || whitelistedInputs.trim().isEmpty()) {
      return new JsonObject().getMap();
    }
    try {
      JsonObject jsonObject = new JsonObject(whitelistedInputs);
      return jsonObject.getMap();
    } catch (Exception e) {
      return new JsonObject().getMap();
    }
  }
}
