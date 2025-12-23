package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BIOMETRIC_BODY_PARAM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.BIOMETRIC_BODY_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_SMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHANNEL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONTACTS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CREDENTIAL_ID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DEVICE_METADATA;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_FLOW;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IDENTIFIER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_IS_NEW_USER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_META_INFO_V2;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_NAME;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_OTP;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_PUBLIC_KEY;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_RESPONSE_TYPE_V2;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SCOPES;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_SIGNATURE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_STATE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_TEMPLATE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERNAME;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_CHALLENGE_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_CREDENTIAL_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_ENCODING;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_SIGNATURE;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_CHALLENGE_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_CLIENT_ID_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_CREDENTIAL_ID_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_CREDENTIAL_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_INVALID_PUBLIC_KEY_FORMAT;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_INVALID_SIGNATURE_ENCODING;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_REFRESH_TOKEN_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SIGNATURE_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SIGNATURE_VERIFICATION_FAILED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_STATE_INVALID_CLIENT_ID_MISMATCH;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_STATE_INVALID_REFRESH_TOKEN_MISMATCH;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_STATE_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_UNAUTHORIZED;
import static com.dreamsportslabs.guardian.Constants.JWT_AMR_HARDWARE_KEY;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIMS_AMR;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.MFA_FACTORS;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNINUP;
import static com.dreamsportslabs.guardian.Constants.PLATFORM_IOS;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_EXPIRES_IN;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_TOKEN_TYPE;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_HEADER_PARAM_SET_COOKIE;
import static com.dreamsportslabs.guardian.Constants.RSA_KEY_PRIVATE_KEY;
import static com.dreamsportslabs.guardian.Constants.RSA_KEY_PUBLIC_KEY;
import static com.dreamsportslabs.guardian.Constants.TEST_PUBLIC_KEY_PATH;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_ID_TOKEN;
import static com.dreamsportslabs.guardian.Constants.TOKEN_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.TOKEN_TYPE_BEARER;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_USERNAME_1;
import static com.dreamsportslabs.guardian.utils.DbUtils.addDefaultClientScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.addFirstPartyClient;
import static com.dreamsportslabs.guardian.utils.DbUtils.addScope;
import static com.dreamsportslabs.guardian.utils.DbUtils.addThirdPartyClient;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import com.dreamsportslabs.guardian.Setup;
import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.BiometricTestUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.clients.jedis.Jedis;

@ExtendWith(Setup.class)
public class V2BiometricCompleteIT {

  private static final String TENANT_ID = "tenant1";
  private static final String TEST_SCOPE = "profile";
  private static String firstPartyClientId;
  private static String thirdPartyClientId;
  private WireMockServer wireMockServer;

  @BeforeAll
  static void setup() {
    addScope(TENANT_ID, TEST_SCOPE);
    firstPartyClientId = addFirstPartyClient(TENANT_ID);
    thirdPartyClientId = addThirdPartyClient(TENANT_ID);
    addDefaultClientScopes(TENANT_ID, firstPartyClientId, TEST_SCOPE);
  }

  // ========== REGISTRATION FLOW TESTS ==========

  @Test
  @DisplayName(
      "V2BiometricComplete - Registration: Should register device and return tokens with valid signature")
  public void registrationShouldSucceedWithValidSignature() throws Exception {
    // Arrange - Get refresh token from passwordless signin
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> userSetup = setupUserAndGetRefreshToken(phoneNumber);
    String refreshToken = (String) userSetup.get("refreshToken");
    StubMapping getUserStub = (StubMapping) userSetup.get("getUserStub");

    // Request challenge
    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse.then().statusCode(SC_OK);

    String challenge = challengeResponse.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state = challengeResponse.jsonPath().getString(BODY_PARAM_STATE);

    // Generate EC key pair and sign challenge
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String signature = BiometricTestUtils.signChallenge(privateKey, challenge);

    // Prepare complete request (registration flow - with public_key)
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, signature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert - Validate response structure
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, notNullValue())
        .body(TOKEN_PARAM_REFRESH_TOKEN, notNullValue())
        .body(TOKEN_PARAM_ID_TOKEN, notNullValue())
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, notNullValue())
        .header(RESPONSE_HEADER_PARAM_SET_COOKIE, notNullValue());

    // Validate cookies match tokens
    String accessToken = response.jsonPath().getString(RESPONSE_BODY_PARAM_ACCESS_TOKEN);
    String newRefreshToken = response.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    assertThat(
        "AT cookie should match access_token", response.getCookie("AT"), equalTo(accessToken));
    assertThat(
        "RT cookie should match refresh_token", response.getCookie("RT"), equalTo(newRefreshToken));

    // Decode and validate JWT claims
    Path publicKeyPath = Paths.get(TEST_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(publicKeyPath));
    Map<String, Object> claims = jwt.getAllClaims();

    // Verify hardware key is in amr claim (hwk = hardware key)
    @SuppressWarnings("unchecked")
    List<String> amr = (List<String>) claims.get(JWT_CLAIMS_AMR);
    assertThat("AMR should contain hwk (hardware key)", amr, hasItem(JWT_AMR_HARDWARE_KEY));

    // Validate MFA factors match client configuration
    @SuppressWarnings("unchecked")
    List<String> mfaFactors = response.jsonPath().getList(MFA_FACTORS);
    assertThat("MFA factors should be present", mfaFactors, notNullValue());
    assertThat("MFA factors should not be empty", mfaFactors.size(), greaterThan(0));

    // Cleanup
    wireMockServer.removeStub(getUserStub);
  }

  @Test
  @DisplayName(
      "V2BiometricComplete - Registration: Should return error when signature verification fails")
  public void registrationShouldFailWithInvalidSignature() throws Exception {
    // Arrange - Get refresh token and challenge
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse.then().statusCode(SC_OK);

    String state = challengeResponse.jsonPath().getString(BODY_PARAM_STATE);

    // Generate EC key pair and sign WRONG data (not the actual challenge)
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String wrongSignature = BiometricTestUtils.createInvalidSignature(privateKey, "wrong_data");

    // Prepare complete request with wrong signature
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, wrongSignature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert - API returns 401 for invalid signature during registration
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_SIGNATURE))
        .body(MESSAGE, equalTo(ERROR_MSG_SIGNATURE_VERIFICATION_FAILED));
  }

  @Test
  @DisplayName(
      "V2BiometricComplete - Registration: Should return error when public_key format is invalid")
  public void registrationShouldFailWithInvalidPublicKeyFormat() throws Exception {
    // Arrange - Get refresh token and challenge
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse.then().statusCode(SC_OK);

    String challenge = challengeResponse.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state = challengeResponse.jsonPath().getString(BODY_PARAM_STATE);

    // Generate valid key pair but use invalid PEM format
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String signature = BiometricTestUtils.signChallenge(privateKey, challenge);
    String invalidPublicKeyPem = BiometricTestUtils.createInvalidPublicKeyPem();

    // Prepare complete request with invalid public key
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, invalidPublicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, signature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert - API returns invalid_request for malformed public key
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_INVALID_PUBLIC_KEY_FORMAT));
  }

  @Test
  @DisplayName(
      "V2BiometricComplete - Registration: Should return error when signature is not valid Base64")
  public void registrationShouldFailWhenSignatureNotBase64() throws Exception {
    // Arrange - Get refresh token and challenge
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse.then().statusCode(SC_OK);

    String state = challengeResponse.jsonPath().getString(BODY_PARAM_STATE);

    // Generate EC key pair
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    String invalidSignature = BiometricTestUtils.createInvalidBase64String();

    // Prepare complete request with invalid signature encoding
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, invalidSignature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_ENCODING))
        .body(MESSAGE, equalTo(ERROR_MSG_INVALID_SIGNATURE_ENCODING));
  }

  @Test
  @DisplayName(
      "V2BiometricComplete - Registration: Should allow re-registration (credential update) for same device")
  public void registrationShouldAllowReregistrationForSameDevice() throws Exception {
    // Arrange - Register device once successfully
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> userSetup = setupUserAndGetRefreshToken(phoneNumber);
    String refreshToken = (String) userSetup.get("refreshToken");
    StubMapping getUserStub = (StubMapping) userSetup.get("getUserStub");

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse1 =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse1.then().statusCode(SC_OK);

    String challenge1 = challengeResponse1.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state1 = challengeResponse1.jsonPath().getString(BODY_PARAM_STATE);

    // First registration
    Map<String, Object> keyPair1 = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem1 = (String) keyPair1.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey1 = (PrivateKey) keyPair1.get(RSA_KEY_PRIVATE_KEY);
    String signature1 = BiometricTestUtils.signChallenge(privateKey1, challenge1);

    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody1 = new HashMap<>();
    completeBody1.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    completeBody1.put(BODY_PARAM_STATE, state1);
    completeBody1.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody1.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody1.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem1);
    completeBody1.put(BODY_PARAM_SIGNATURE, signature1);
    completeBody1.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    Response response1 = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody1);
    response1.then().statusCode(SC_OK);

    // Attempt second registration with same device but different key
    Response challengeResponse2 =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse2.then().statusCode(SC_OK);

    String challenge2 = challengeResponse2.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state2 = challengeResponse2.jsonPath().getString(BODY_PARAM_STATE);

    // Generate NEW EC key pair (different from original)
    Map<String, Object> keyPair2 = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem2 = (String) keyPair2.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey2 = (PrivateKey) keyPair2.get(RSA_KEY_PRIVATE_KEY);
    String signature2 = BiometricTestUtils.signChallenge(privateKey2, challenge2);

    Map<String, Object> completeBody2 = new HashMap<>();
    completeBody2.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    completeBody2.put(BODY_PARAM_STATE, state2);
    completeBody2.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody2.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody2.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem2);
    completeBody2.put(BODY_PARAM_SIGNATURE, signature2);
    completeBody2.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act - Attempt second registration (credential update)
    Response response2 = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody2);

    // Assert - API allows re-registration (updates the credential with new public key)
    response2
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, notNullValue())
        .body(TOKEN_PARAM_REFRESH_TOKEN, notNullValue());

    // Cleanup
    wireMockServer.removeStub(getUserStub);
  }

  @Test
  @DisplayName(
      "V2BiometricComplete - Registration: Should return error when public key algorithm is not ES256")
  public void registrationShouldFailWithNonES256Key() throws Exception {
    // Arrange - Get refresh token and challenge
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse.then().statusCode(SC_OK);

    String state = challengeResponse.jsonPath().getString(BODY_PARAM_STATE);

    // Generate RSA key pair instead of EC
    String rsaPublicKeyPem = BiometricTestUtils.generateRSAPublicKeyPem();
    String dummySignature = "dummySignature";

    // Prepare complete request with RSA public key
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, rsaPublicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, dummySignature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert - API returns invalid_request for non-ES256 public key
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_INVALID_PUBLIC_KEY_FORMAT));
  }

  // ========== LOGIN FLOW TESTS ==========

  @Test
  @DisplayName(
      "V2BiometricComplete - Login: Should authenticate and return tokens with valid registered device")
  public void loginShouldSucceedWithRegisteredDevice() throws Exception {
    // Arrange - Step 1: Register device first
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> userSetup = setupUserAndGetRefreshToken(phoneNumber);
    String refreshToken = (String) userSetup.get("refreshToken");
    StubMapping getUserStub = (StubMapping) userSetup.get("getUserStub");

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);

    // Get challenge for registration
    Response challengeResponse1 =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse1.then().statusCode(SC_OK);

    String challenge1 = challengeResponse1.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state1 = challengeResponse1.jsonPath().getString(BODY_PARAM_STATE);

    // Generate EC key pair and register
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String signature1 = BiometricTestUtils.signChallenge(privateKey, challenge1);

    String credentialId = generateRandomCredentialId();
    Map<String, Object> registrationBody = new HashMap<>();
    registrationBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    registrationBody.put(BODY_PARAM_STATE, state1);
    registrationBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    registrationBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    registrationBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    registrationBody.put(BODY_PARAM_SIGNATURE, signature1);
    registrationBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    Response registrationResponse =
        ApplicationIoUtils.v2BiometricComplete(TENANT_ID, registrationBody);
    registrationResponse.then().statusCode(SC_OK);

    // Step 2: Get new challenge for login
    Response challengeResponse2 =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse2.then().statusCode(SC_OK);

    String challenge2 = challengeResponse2.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state2 = challengeResponse2.jsonPath().getString(BODY_PARAM_STATE);

    // Step 3: Sign challenge with previously registered private key
    String signature2 = BiometricTestUtils.signChallenge(privateKey, challenge2);

    // Prepare login request (WITHOUT public_key)
    Map<String, Object> loginBody = new HashMap<>();
    loginBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    loginBody.put(BODY_PARAM_STATE, state2);
    loginBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    loginBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    // NO public_key - this is login flow
    loginBody.put(BODY_PARAM_SIGNATURE, signature2);
    loginBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, loginBody);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body(RESPONSE_BODY_PARAM_ACCESS_TOKEN, notNullValue())
        .body(TOKEN_PARAM_REFRESH_TOKEN, notNullValue())
        .body(TOKEN_PARAM_ID_TOKEN, notNullValue())
        .body(RESPONSE_BODY_PARAM_TOKEN_TYPE, equalTo(TOKEN_TYPE_BEARER))
        .body(RESPONSE_BODY_PARAM_EXPIRES_IN, notNullValue())
        .header(RESPONSE_HEADER_PARAM_SET_COOKIE, notNullValue());

    // Validate cookies
    String accessToken = response.jsonPath().getString(RESPONSE_BODY_PARAM_ACCESS_TOKEN);
    String newRefreshToken = response.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    assertThat(
        "AT cookie should match access_token", response.getCookie("AT"), equalTo(accessToken));
    assertThat(
        "RT cookie should match refresh_token", response.getCookie("RT"), equalTo(newRefreshToken));

    // Decode and validate JWT claims
    Path publicKeyPath = Paths.get(TEST_PUBLIC_KEY_PATH);
    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(publicKeyPath));
    Map<String, Object> claims = jwt.getAllClaims();

    // Verify hardware key is in amr claim (hwk = hardware key)
    @SuppressWarnings("unchecked")
    List<String> amr = (List<String>) claims.get(JWT_CLAIMS_AMR);
    assertThat("AMR should contain hwk (hardware key)", amr, hasItem(JWT_AMR_HARDWARE_KEY));

    // Validate MFA factors match client configuration
    @SuppressWarnings("unchecked")
    List<String> mfaFactorsLogin = response.jsonPath().getList(MFA_FACTORS);
    assertThat("MFA factors should be present", mfaFactorsLogin, notNullValue());
    assertThat("MFA factors should not be empty", mfaFactorsLogin.size(), greaterThan(0));

    // Cleanup
    wireMockServer.removeStub(getUserStub);
  }

  @Test
  @DisplayName("V2BiometricComplete - Login: Should return error when device is not registered")
  public void loginShouldFailWhenDeviceNotRegistered() throws Exception {
    // Arrange - Get refresh token and challenge with NEW (unregistered) device
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse.then().statusCode(SC_OK);

    String challenge = challengeResponse.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state = challengeResponse.jsonPath().getString(BODY_PARAM_STATE);

    // Generate signature but device is not registered
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String signature = BiometricTestUtils.signChallenge(privateKey, challenge);

    // Prepare login request without public_key (login flow)
    String credentialId = generateRandomCredentialId();
    Map<String, Object> loginBody = new HashMap<>();
    loginBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    loginBody.put(BODY_PARAM_STATE, state);
    loginBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    loginBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    // NO public_key
    loginBody.put(BODY_PARAM_SIGNATURE, signature);
    loginBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, loginBody);

    // Assert - 404 is correct since credential is not found
    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CREDENTIAL_NOT_FOUND))
        .body(MESSAGE, equalTo(ERROR_MSG_CREDENTIAL_NOT_FOUND));
  }

  @Test
  @DisplayName(
      "V2BiometricComplete - Login: Should return error when signature doesn't match stored public key")
  public void loginShouldFailWithWrongSignature() throws Exception {
    // Arrange - Step 1: Register device with one key pair
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> userSetup = setupUserAndGetRefreshToken(phoneNumber);
    String refreshToken = (String) userSetup.get("refreshToken");
    StubMapping getUserStub = (StubMapping) userSetup.get("getUserStub");

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);

    // Register with first key pair
    Response challengeResponse1 =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse1.then().statusCode(SC_OK);

    String challenge1 = challengeResponse1.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state1 = challengeResponse1.jsonPath().getString(BODY_PARAM_STATE);

    Map<String, Object> keyPair1 = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem1 = (String) keyPair1.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey1 = (PrivateKey) keyPair1.get(RSA_KEY_PRIVATE_KEY);
    String signature1 = BiometricTestUtils.signChallenge(privateKey1, challenge1);

    String credentialId = generateRandomCredentialId();
    Map<String, Object> registrationBody = new HashMap<>();
    registrationBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    registrationBody.put(BODY_PARAM_STATE, state1);
    registrationBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    registrationBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    registrationBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem1);
    registrationBody.put(BODY_PARAM_SIGNATURE, signature1);
    registrationBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    Response registrationResponse =
        ApplicationIoUtils.v2BiometricComplete(TENANT_ID, registrationBody);
    registrationResponse.then().statusCode(SC_OK);

    // Step 2: Get new challenge
    Response challengeResponse2 =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse2.then().statusCode(SC_OK);

    String challenge2 = challengeResponse2.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state2 = challengeResponse2.jsonPath().getString(BODY_PARAM_STATE);

    // Step 3: Sign challenge with DIFFERENT private key
    Map<String, Object> keyPair2 = BiometricTestUtils.generateECKeyPair();
    PrivateKey privateKey2 = (PrivateKey) keyPair2.get(RSA_KEY_PRIVATE_KEY);
    String signature2 = BiometricTestUtils.signChallenge(privateKey2, challenge2);

    // Prepare login request with wrong signature
    Map<String, Object> loginBody = new HashMap<>();
    loginBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    loginBody.put(BODY_PARAM_STATE, state2);
    loginBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    loginBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    loginBody.put(BODY_PARAM_SIGNATURE, signature2);
    loginBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, loginBody);

    // Assert - Wrong signature returns 401
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_SIGNATURE))
        .body(MESSAGE, equalTo(ERROR_MSG_SIGNATURE_VERIFICATION_FAILED));

    // Cleanup
    wireMockServer.removeStub(getUserStub);
  }

  @Test
  @DisplayName(
      "V2BiometricComplete - Login: Should return error when using different device than registered")
  public void loginShouldFailWithDifferentDevice() throws Exception {
    // Arrange - Step 1: Register device A
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> userSetup = setupUserAndGetRefreshToken(phoneNumber);
    String refreshToken = (String) userSetup.get("refreshToken");
    StubMapping getUserStub = (StubMapping) userSetup.get("getUserStub");

    String deviceIdA = generateRandomDeviceId();
    Map<String, Object> deviceMetadataA = createDeviceMetadata(PLATFORM_IOS, deviceIdA);

    Response challengeResponse1 =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadataA);
    challengeResponse1.then().statusCode(SC_OK);

    String challenge1 = challengeResponse1.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state1 = challengeResponse1.jsonPath().getString(BODY_PARAM_STATE);

    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String signature1 = BiometricTestUtils.signChallenge(privateKey, challenge1);

    String credentialId = generateRandomCredentialId();
    Map<String, Object> registrationBody = new HashMap<>();
    registrationBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    registrationBody.put(BODY_PARAM_STATE, state1);
    registrationBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    registrationBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    registrationBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    registrationBody.put(BODY_PARAM_SIGNATURE, signature1);
    registrationBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadataA);

    Response registrationResponse =
        ApplicationIoUtils.v2BiometricComplete(TENANT_ID, registrationBody);
    registrationResponse.then().statusCode(SC_OK);

    // Step 2: Get challenge for device B (different device)
    String deviceIdB = generateRandomDeviceId();
    Map<String, Object> deviceMetadataB = createDeviceMetadata(PLATFORM_IOS, deviceIdB);

    Response challengeResponse2 =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadataB);
    challengeResponse2.then().statusCode(SC_OK);

    String challenge2 = challengeResponse2.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state2 = challengeResponse2.jsonPath().getString(BODY_PARAM_STATE);

    // Step 3: Generate different key pair for device B and sign challenge
    Map<String, Object> keyPairB = BiometricTestUtils.generateECKeyPair();
    PrivateKey privateKeyB = (PrivateKey) keyPairB.get(RSA_KEY_PRIVATE_KEY);
    String signature2 = BiometricTestUtils.signChallenge(privateKeyB, challenge2);

    Map<String, Object> loginBody = new HashMap<>();
    loginBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    loginBody.put(BODY_PARAM_STATE, state2);
    loginBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    loginBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    loginBody.put(BODY_PARAM_SIGNATURE, signature2);
    loginBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadataB);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, loginBody);

    // Assert - Device B not registered, should return 404
    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CREDENTIAL_NOT_FOUND))
        .body(MESSAGE, equalTo(ERROR_MSG_CREDENTIAL_NOT_FOUND));

    // Cleanup
    wireMockServer.removeStub(getUserStub);
  }

  // ========== COMMON VALIDATION TESTS ==========

  @Test
  @DisplayName("V2BiometricComplete - Should return error when state is invalid")
  public void shouldReturnErrorWhenStateInvalid() throws Exception {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);

    // Generate key pair and signature
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String signature = BiometricTestUtils.signChallenge(privateKey, "dummyChallenge");

    // Prepare request with invalid state
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    completeBody.put(BODY_PARAM_STATE, "invalid_state_12345");
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, signature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CHALLENGE_NOT_FOUND))
        .body(MESSAGE, equalTo(ERROR_MSG_CHALLENGE_NOT_FOUND));
  }

  @Test
  @DisplayName("V2BiometricComplete - Should return error when state has expired")
  public void shouldReturnErrorWhenStateExpired() throws Exception {
    // Arrange - Get challenge with state
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse.then().statusCode(SC_OK);

    String challenge = challengeResponse.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state = challengeResponse.jsonPath().getString(BODY_PARAM_STATE);

    // Delete state from Redis to simulate expiry
    deleteStateFromRedis(state);

    // Generate key pair and signature
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String signature = BiometricTestUtils.signChallenge(privateKey, challenge);

    // Prepare request
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, signature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CHALLENGE_NOT_FOUND))
        .body(MESSAGE, equalTo(ERROR_MSG_CHALLENGE_NOT_FOUND));
  }

  @Test
  @DisplayName("V2BiometricComplete - Should return error when state is missing")
  public void shouldReturnErrorWhenStateMissing() throws Exception {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);

    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    String dummySignature = "dummySignature";

    // Prepare request WITHOUT state
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    // NO state
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, dummySignature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_STATE_REQUIRED));
  }

  @Test
  @DisplayName("V2BiometricComplete - Should return error when client_id doesn't match challenge")
  public void shouldReturnErrorWhenClientIdMismatch() throws Exception {
    // Arrange - Get challenge with clientA
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse.then().statusCode(SC_OK);

    String challenge = challengeResponse.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state = challengeResponse.jsonPath().getString(BODY_PARAM_STATE);

    // Generate key pair and signature
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String signature = BiometricTestUtils.signChallenge(privateKey, challenge);

    // Act - Call v2BiometricComplete with different client_id (clientB)
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, "different_client_id");
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, signature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, notNullValue())
        .body(MESSAGE, equalTo(ERROR_MSG_STATE_INVALID_CLIENT_ID_MISMATCH));
  }

  @Test
  @DisplayName(
      "V2BiometricComplete - Should return error when refresh_token doesn't match challenge")
  public void shouldReturnErrorWhenRefreshTokenMismatch() throws Exception {
    // Arrange - Get challenge with refreshTokenA
    String phoneNumber1 = generateRandomPhoneNumber();
    StubMapping getUserStub1 = stubGetUserForPasswordless(phoneNumber1, null, false);

    Response passwordlessResponse1 = performPasswordlessSignin(phoneNumber1);
    String refreshToken1 = passwordlessResponse1.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub1);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse =
        requestBiometricChallenge(refreshToken1, firstPartyClientId, deviceMetadata);
    challengeResponse.then().statusCode(SC_OK);

    String challenge = challengeResponse.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state = challengeResponse.jsonPath().getString(BODY_PARAM_STATE);

    // Get different refreshTokenB
    String phoneNumber2 = generateRandomPhoneNumber();
    StubMapping getUserStub2 = stubGetUserForPasswordless(phoneNumber2, null, false);

    Response passwordlessResponse2 = performPasswordlessSignin(phoneNumber2);
    String refreshToken2 = passwordlessResponse2.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub2);

    // Generate key pair and signature
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String signature = BiometricTestUtils.signChallenge(privateKey, challenge);

    // Act - Call v2BiometricComplete with refreshTokenB
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken2);
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, signature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, notNullValue())
        .body(MESSAGE, equalTo(ERROR_MSG_STATE_INVALID_REFRESH_TOKEN_MISMATCH));
  }

  @Test
  @DisplayName("V2BiometricComplete - Should return error when refresh_token is invalid")
  public void shouldReturnErrorWhenRefreshTokenInvalid() throws Exception {
    // Arrange - Get valid challenge
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse.then().statusCode(SC_OK);

    String challenge = challengeResponse.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state = challengeResponse.jsonPath().getString(BODY_PARAM_STATE);

    // Generate key pair and signature
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String signature = BiometricTestUtils.signChallenge(privateKey, challenge);

    // Act - Call with completely invalid refresh token
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, "invalid_refresh_token_xyz");
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, signature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert - API returns 400 for invalid refresh token
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, notNullValue())
        .body(MESSAGE, equalTo(ERROR_MSG_STATE_INVALID_REFRESH_TOKEN_MISMATCH));
  }

  @Test
  @DisplayName("V2BiometricComplete - Should return error when refresh_token is missing")
  public void shouldReturnErrorWhenRefreshTokenMissing() throws Exception {
    // Arrange
    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);

    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    String dummySignature = "dummySignature";

    // Prepare request WITHOUT refresh_token
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    // NO refresh_token
    completeBody.put(BODY_PARAM_STATE, "some_state");
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, dummySignature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_REFRESH_TOKEN_REQUIRED));
  }

  @Test
  @DisplayName("V2BiometricComplete - Should return error when client_id is missing")
  public void shouldReturnErrorWhenClientIdMissing() throws Exception {
    // Arrange
    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);

    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    String dummySignature = "dummySignature";

    // Prepare request WITHOUT client_id
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, "some_token");
    completeBody.put(BODY_PARAM_STATE, "some_state");
    // NO client_id
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, dummySignature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_CLIENT_ID_REQUIRED));
  }

  @Test
  @DisplayName("V2BiometricComplete - Should return error when credential_id is missing")
  public void shouldReturnErrorWhenCredentialIdMissing() throws Exception {
    // Arrange
    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);

    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    String dummySignature = "dummySignature";

    // Prepare request WITHOUT credential_id
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, "some_token");
    completeBody.put(BODY_PARAM_STATE, "some_state");
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    // NO credential_id
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, dummySignature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_CREDENTIAL_ID_REQUIRED));
  }

  @Test
  @DisplayName("V2BiometricComplete - Should return error when signature is missing")
  public void shouldReturnErrorWhenSignatureMissing() throws Exception {
    // Arrange
    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);

    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);

    // Prepare request WITHOUT signature
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, "some_token");
    completeBody.put(BODY_PARAM_STATE, "some_state");
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    // NO signature
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_SIGNATURE_REQUIRED));
  }

  @Test
  @DisplayName("V2BiometricComplete - Should return error when device_metadata is missing")
  public void shouldReturnErrorWhenDeviceMetadataMissing() throws Exception {
    // Arrange
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    String dummySignature = "dummySignature";

    // Prepare request WITHOUT device_metadata
    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, "some_token");
    completeBody.put(BODY_PARAM_STATE, "some_state");
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, dummySignature);
    // NO device_metadata

    // Act
    Response response = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("device_metadata is required"));
  }

  @Test
  @DisplayName("V2BiometricComplete - Should return error when tenant_id header is missing")
  public void shouldReturnErrorWhenTenantIdMissing() throws Exception {
    // Arrange
    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);

    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    String dummySignature = "dummySignature";

    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, "some_token");
    completeBody.put(BODY_PARAM_STATE, "some_state");
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, dummySignature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act - Pass null for tenant ID
    Response response = ApplicationIoUtils.v2BiometricComplete(null, completeBody);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, notNullValue())
        .body(MESSAGE, equalTo(ERROR_MSG_UNAUTHORIZED));
  }

  @Test
  @DisplayName("V2BiometricComplete - Should not allow reusing same state for multiple completions")
  public void shouldNotAllowReusingState() throws Exception {
    // Arrange - Get challenge and complete successfully once
    String phoneNumber = generateRandomPhoneNumber();
    Map<String, Object> userSetup = setupUserAndGetRefreshToken(phoneNumber);
    String refreshToken = (String) userSetup.get("refreshToken");
    StubMapping getUserStub = (StubMapping) userSetup.get("getUserStub");

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);
    Response challengeResponse =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse.then().statusCode(SC_OK);

    String challenge = challengeResponse.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state = challengeResponse.jsonPath().getString(BODY_PARAM_STATE);

    // Generate key pair and signature
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get(RSA_KEY_PUBLIC_KEY);
    PrivateKey privateKey = (PrivateKey) keyPair.get(RSA_KEY_PRIVATE_KEY);
    String signature = BiometricTestUtils.signChallenge(privateKey, challenge);

    String credentialId = generateRandomCredentialId();
    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    completeBody.put(BODY_PARAM_CREDENTIAL_ID, credentialId);
    completeBody.put(BODY_PARAM_PUBLIC_KEY, publicKeyPem);
    completeBody.put(BODY_PARAM_SIGNATURE, signature);
    completeBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // First completion - should succeed
    Response response1 = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);
    response1.then().statusCode(SC_OK);

    // Act - Try to complete again with same state
    Response response2 = ApplicationIoUtils.v2BiometricComplete(TENANT_ID, completeBody);

    // Assert - State should not be found (already consumed)
    response2
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CHALLENGE_NOT_FOUND))
        .body(MESSAGE, equalTo(ERROR_MSG_CHALLENGE_NOT_FOUND));

    // Cleanup
    wireMockServer.removeStub(getUserStub);
  }

  // ========== Helper Methods ==========

  /**
   * Performs passwordless signin to get a refresh token
   *
   * @param phoneNumber Phone number for passwordless signin
   * @return Passwordless complete response containing refresh token
   */
  private Response performPasswordlessSignin(String phoneNumber) {
    Map<String, Object> initBody = getPasswordlessInitRequestBody(phoneNumber, BODY_CHANNEL_SMS);
    Response initResponse = ApplicationIoUtils.v2PasswordlessInit(TENANT_ID, initBody);
    initResponse.then().statusCode(SC_OK);
    String state = initResponse.jsonPath().getString(BODY_PARAM_STATE);

    Map<String, Object> completeBody = new HashMap<>();
    completeBody.put(BODY_PARAM_STATE, state);
    completeBody.put(BODY_PARAM_OTP, "999999");

    Response passwordlessResponse =
        ApplicationIoUtils.v2PasswordlessComplete(TENANT_ID, completeBody);
    passwordlessResponse.then().statusCode(SC_OK);
    return passwordlessResponse;
  }

  /**
   * Helper to setup a user and get refresh token with active stub for biometric tests
   *
   * @param phoneNumber Phone number for the user
   * @return Map containing "refreshToken" and "getUserStub"
   */
  private Map<String, Object> setupUserAndGetRefreshToken(String phoneNumber) {
    // Create user stub
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    // Perform passwordless signin
    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString(TOKEN_PARAM_REFRESH_TOKEN);

    // Keep the stub active for biometric complete calls
    Map<String, Object> result = new HashMap<>();
    result.put("refreshToken", refreshToken);
    result.put("getUserStub", getUserStub);
    return result;
  }

  /** Helper to build passwordless init request body */
  private Map<String, Object> getPasswordlessInitRequestBody(String identifier, String channel) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    requestBody.put(BODY_PARAM_SCOPES, List.of(TEST_SCOPE));
    requestBody.put(BODY_PARAM_FLOW, PASSWORDLESS_FLOW_SIGNINUP);
    requestBody.put(BODY_PARAM_RESPONSE_TYPE_V2, BODY_PARAM_RESPONSE_TYPE_TOKEN);
    requestBody.put(BODY_PARAM_META_INFO_V2, getMetaInfo());

    Map<String, Object> contact = new HashMap<>();
    contact.put(BODY_PARAM_CHANNEL, channel);
    contact.put(BODY_PARAM_IDENTIFIER, identifier);
    contact.put(BODY_PARAM_TEMPLATE, getTemplate());
    requestBody.put(BODY_PARAM_CONTACTS, List.of(contact));

    return requestBody;
  }

  /** Helper to get meta info for passwordless */
  private Map<String, Object> getMetaInfo() {
    Map<String, Object> metaInfo = new HashMap<>();
    metaInfo.put("deviceName", "testDevice");
    metaInfo.put("location", "testLocation");
    return metaInfo;
  }

  /** Helper to get template for passwordless */
  private Map<String, Object> getTemplate() {
    Map<String, Object> template = new HashMap<>();
    template.put(BODY_PARAM_NAME, "otp");
    return template;
  }

  /**
   * Requests a biometric challenge
   *
   * @param refreshToken Refresh token
   * @param clientId Client ID
   * @param deviceMetadata Device metadata
   * @return Challenge response
   */
  private Response requestBiometricChallenge(
      String refreshToken, String clientId, Map<String, Object> deviceMetadata) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, clientId);
    requestBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);
    return ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);
  }

  /**
   * Deletes biometric challenge state from Redis (to simulate expiry)
   *
   * @param state State to delete
   */
  private void deleteStateFromRedis(String state) {
    String key = "BIOMETRIC_CHALLENGE_" + TENANT_ID + "_" + state;
    try (Jedis jedis = new Jedis("localhost", 6379)) {
      jedis.del(key);
    }
  }

  /** Stub for getUser call during passwordless init */
  private StubMapping stubGetUserForPasswordless(
      String phoneNumber, String email, boolean isNewUser) {
    JsonObject responseBody = new JsonObject();
    if (isNewUser) {
      responseBody.put(BODY_PARAM_IS_NEW_USER, true);
    } else {
      String userId = RandomStringUtils.randomAlphanumeric(10);
      responseBody.put(BODY_PARAM_USERID, userId);
      responseBody.put(BODY_PARAM_USERNAME, V2_SIGNIN_TEST_USERNAME_1);
      responseBody.put(BODY_PARAM_NAME, "John Doe");
      if (phoneNumber != null) {
        responseBody.put(BODY_PARAM_PHONE_NUMBER, phoneNumber);
      }
      if (email != null) {
        responseBody.put(BODY_PARAM_EMAIL, email);
      }
    }

    return wireMockServer.stubFor(
        get(urlPathMatching("/user"))
            .willReturn(
                aResponse()
                    .withStatus(SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody.encode())));
  }

  /** Creates device metadata map for biometric requests */
  private Map<String, Object> createDeviceMetadata(String platform, String deviceId) {
    Map<String, Object> deviceMetadata = new HashMap<>();
    deviceMetadata.put("platform", platform);
    deviceMetadata.put("device_id", deviceId);
    deviceMetadata.put("device_model", "TestModel");
    deviceMetadata.put("os_version", "14.0");
    deviceMetadata.put("app_version", "1.0.0");
    deviceMetadata.put("device_name", "TestDevice");
    return deviceMetadata;
  }

  /** Generates a random phone number for testing */
  private String generateRandomPhoneNumber() {
    return "9" + String.format("%09d", (int) (Math.random() * 1000000000));
  }

  /** Generates a random email for testing */
  private String generateRandomEmail() {
    return "test" + System.currentTimeMillis() + "@example.com";
  }

  /** Generates a random device ID for testing */
  private String generateRandomDeviceId() {
    return "device_" + RandomStringUtils.randomAlphanumeric(16);
  }

  /** Generates a random credential ID for testing */
  private String generateRandomCredentialId() {
    return "cred_" + RandomStringUtils.randomAlphanumeric(32);
  }
}
