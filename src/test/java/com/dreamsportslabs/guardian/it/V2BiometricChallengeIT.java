package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.BIOMETRIC_BODY_PARAM_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.BIOMETRIC_BODY_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BIOMETRIC_CHALLENGE_EXPIRY_SECONDS;
import static com.dreamsportslabs.guardian.Constants.BODY_CHANNEL_SMS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHALLENGE;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CHANNEL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CONTACTS;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_CREDENTIAL_ID;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_DEVICE_METADATA;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_EXPIRES_IN;
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
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_CLIENT_ID_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_DEVICE_ID_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_DEVICE_METADATA_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_INVALID_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_PLATFORM_INVALID;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_REFRESH_TOKEN_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_UNAUTHORIZED;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.PASSWORDLESS_FLOW_SIGNINUP;
import static com.dreamsportslabs.guardian.Constants.PLATFORM_INVALID;
import static com.dreamsportslabs.guardian.Constants.PLATFORM_IOS;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_USERNAME_1;
import static com.dreamsportslabs.guardian.utils.DbUtils.addDefaultClientScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.addFirstPartyClient;
import static com.dreamsportslabs.guardian.utils.DbUtils.addScope;
import static com.dreamsportslabs.guardian.utils.DbUtils.addThirdPartyClient;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.dreamsportslabs.guardian.Setup;
import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.BiometricTestUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Setup.class)
public class V2BiometricChallengeIT {

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

  @Test
  @DisplayName(
      "V2BiometricChallenge - Should return challenge and state when valid refresh token is provided")
  public void shouldReturnChallengeWhenValidRefreshTokenProvided() {
    // Arrange - Get valid refresh token from passwordless signin
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    // Prepare request
    String deviceId = generateRandomDeviceId();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    requestBody.put(BODY_PARAM_DEVICE_METADATA, createDeviceMetadata(PLATFORM_IOS, deviceId));

    // Act
    Response response = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_CHALLENGE, notNullValue())
        .body(BODY_PARAM_STATE, notNullValue())
        .body(BODY_PARAM_EXPIRES_IN, equalTo(BIOMETRIC_CHALLENGE_EXPIRY_SECONDS))
        .body(BODY_PARAM_CREDENTIAL_ID, nullValue());

    // Validate challenge is valid Base64
    String challenge = response.jsonPath().getString(BODY_PARAM_CHALLENGE);
    assertThat("Challenge should be valid Base64", isValidBase64(challenge), is(true));
  }

  @Test
  @DisplayName(
      "V2BiometricChallenge - Should return existing credential_id when device is already registered")
  public void shouldReturnExistingCredentialIdWhenDeviceRegistered() throws Exception {
    // Arrange - Step 1: Register device with biometric credential
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");

    String deviceId = generateRandomDeviceId();
    Map<String, Object> deviceMetadata = createDeviceMetadata(PLATFORM_IOS, deviceId);

    // Get challenge for registration
    Response challengeResponse1 =
        requestBiometricChallenge(refreshToken, firstPartyClientId, deviceMetadata);
    challengeResponse1.then().statusCode(SC_OK);

    String challenge1 = challengeResponse1.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state1 = challengeResponse1.jsonPath().getString(BODY_PARAM_STATE);

    // Generate EC key pair and register device
    Map<String, Object> keyPair = BiometricTestUtils.generateECKeyPair();
    String publicKeyPem = (String) keyPair.get("publicKey");
    PrivateKey privateKey = (PrivateKey) keyPair.get("privateKey");
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

    // Act - Step 2: Request challenge again for same registered device
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    requestBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    Response response = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert - Should return challenge, state, AND existing credential_id
    response
        .then()
        .statusCode(SC_OK)
        .body(BODY_PARAM_CHALLENGE, notNullValue())
        .body(BODY_PARAM_STATE, notNullValue())
        .body(BODY_PARAM_EXPIRES_IN, equalTo(BIOMETRIC_CHALLENGE_EXPIRY_SECONDS))
        .body(BODY_PARAM_CREDENTIAL_ID, equalTo(credentialId));

    // Validate challenge is valid Base64
    String challenge = response.jsonPath().getString(BODY_PARAM_CHALLENGE);
    assertThat("Challenge should be valid Base64", isValidBase64(challenge), is(true));

    // Cleanup
    wireMockServer.removeStub(getUserStub);
  }

  @Test
  @DisplayName("V2BiometricChallenge - Should return error when refresh token is invalid")
  public void shouldReturnErrorWhenRefreshTokenInvalid() {
    // Arrange
    String deviceId = generateRandomDeviceId();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, "invalid_refresh_token");
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    requestBody.put(BODY_PARAM_DEVICE_METADATA, createDeviceMetadata(PLATFORM_IOS, deviceId));

    // Act
    Response response = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, notNullValue())
        .body(MESSAGE, equalTo(ERROR_MSG_INVALID_REFRESH_TOKEN));
  }

  @Test
  @DisplayName("V2BiometricChallenge - Should return error when refresh token is missing")
  public void shouldReturnErrorWhenRefreshTokenMissing() {
    // Arrange
    String deviceId = generateRandomDeviceId();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    requestBody.put(BODY_PARAM_DEVICE_METADATA, createDeviceMetadata(PLATFORM_IOS, deviceId));

    // Act
    Response response = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_REFRESH_TOKEN_REQUIRED));
  }

  @Test
  @DisplayName("V2BiometricChallenge - Should return error when client_id is missing")
  public void shouldReturnErrorWhenClientIdMissing() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    requestBody.put(BODY_PARAM_DEVICE_METADATA, createDeviceMetadata(PLATFORM_IOS, deviceId));

    // Act
    Response response = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_CLIENT_ID_REQUIRED));
  }

  @Test
  @DisplayName("V2BiometricChallenge - Should return error when client_id is invalid")
  public void shouldReturnErrorWhenClientIdInvalid() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, "invalid_client_id");
    requestBody.put(BODY_PARAM_DEVICE_METADATA, createDeviceMetadata(PLATFORM_IOS, deviceId));

    // Act
    Response response = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, notNullValue())
        .body(MESSAGE, equalTo(ERROR_MSG_INVALID_REFRESH_TOKEN));
  }

  @Test
  @DisplayName("V2BiometricChallenge - Should return error when device_metadata is missing")
  public void shouldReturnErrorWhenDeviceMetadataMissing() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);

    // Act
    Response response = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo(ERROR_MSG_DEVICE_METADATA_REQUIRED));
  }

  @Test
  @DisplayName(
      "V2BiometricChallenge - Should return error when device_metadata.platform is invalid")
  public void shouldReturnErrorWhenPlatformInvalid() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    requestBody.put(BODY_PARAM_DEVICE_METADATA, createDeviceMetadata(PLATFORM_INVALID, deviceId));

    // Act
    Response response = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, notNullValue())
        .body(MESSAGE, equalTo(ERROR_MSG_PLATFORM_INVALID));
  }

  @Test
  @DisplayName(
      "V2BiometricChallenge - Should return error when device_metadata.device_id is missing")
  public void shouldReturnErrorWhenDeviceIdMissing() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    Map<String, Object> deviceMetadata = new HashMap<>();
    deviceMetadata.put("platform", PLATFORM_IOS);
    // device_id is missing

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    requestBody.put(BODY_PARAM_DEVICE_METADATA, deviceMetadata);

    // Act
    Response response = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, notNullValue())
        .body(MESSAGE, equalTo(ERROR_MSG_DEVICE_ID_REQUIRED));
  }

  @Test
  @DisplayName("V2BiometricChallenge - Should generate different challenge on each request")
  public void shouldGenerateDifferentChallengeOnEachRequest() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    requestBody.put(BODY_PARAM_DEVICE_METADATA, createDeviceMetadata(PLATFORM_IOS, deviceId));

    // Act - Request challenge twice
    Response response1 = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);
    Response response2 = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert
    response1.then().statusCode(SC_OK);
    response2.then().statusCode(SC_OK);

    String challenge1 = response1.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String challenge2 = response2.jsonPath().getString(BODY_PARAM_CHALLENGE);
    String state1 = response1.jsonPath().getString(BODY_PARAM_STATE);
    String state2 = response2.jsonPath().getString(BODY_PARAM_STATE);

    assertThat("Challenges should be different", challenge1, not(equalTo(challenge2)));
    assertThat("States should be different", state1, not(equalTo(state2)));
  }

  @Test
  @DisplayName("V2BiometricChallenge - Should return error when using third party client")
  public void shouldReturnErrorWhenUsingThirdPartyClient() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    // Get refresh token using first party client
    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, thirdPartyClientId); // Using third party client
    requestBody.put(BODY_PARAM_DEVICE_METADATA, createDeviceMetadata(PLATFORM_IOS, deviceId));

    // Act
    Response response = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert - Third party clients should not be able to use biometric APIs
    response
        .then()
        .statusCode(anyOf(equalTo(SC_UNAUTHORIZED), equalTo(SC_BAD_REQUEST)))
        .rootPath(ERROR)
        .body(CODE, notNullValue())
        .body(MESSAGE, equalTo(ERROR_MSG_INVALID_REFRESH_TOKEN));
  }

  @Test
  @DisplayName("V2BiometricChallenge - Should return error when tenant_id header is missing")
  public void shouldReturnErrorWhenTenantIdMissing() {
    // Arrange
    String deviceId = generateRandomDeviceId();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, "some_token");
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    requestBody.put(BODY_PARAM_DEVICE_METADATA, createDeviceMetadata(PLATFORM_IOS, deviceId));

    // Act - Pass null for tenant ID
    Response response = ApplicationIoUtils.v2BiometricChallenge(null, requestBody);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .rootPath(ERROR)
        .body(CODE, notNullValue())
        .body(MESSAGE, equalTo(ERROR_MSG_UNAUTHORIZED));
  }

  @Test
  @DisplayName("V2BiometricChallenge - Should validate challenge is valid Base64")
  public void shouldValidateChallengeIsValidBase64() {
    // Arrange
    String phoneNumber = generateRandomPhoneNumber();
    StubMapping getUserStub = stubGetUserForPasswordless(phoneNumber, null, false);

    Response passwordlessResponse = performPasswordlessSignin(phoneNumber);
    String refreshToken = passwordlessResponse.jsonPath().getString("refresh_token");
    wireMockServer.removeStub(getUserStub);

    String deviceId = generateRandomDeviceId();
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(BIOMETRIC_BODY_PARAM_REFRESH_TOKEN, refreshToken);
    requestBody.put(BIOMETRIC_BODY_PARAM_CLIENT_ID, firstPartyClientId);
    requestBody.put(BODY_PARAM_DEVICE_METADATA, createDeviceMetadata(PLATFORM_IOS, deviceId));

    // Act
    Response response = ApplicationIoUtils.v2BiometricChallenge(TENANT_ID, requestBody);

    // Assert
    response.then().statusCode(SC_OK);
    String challenge = response.jsonPath().getString(BODY_PARAM_CHALLENGE);
    assertThat("Challenge should be valid Base64", isValidBase64(challenge), is(true));
    assertThat("Challenge should not be empty", challenge.length(), greaterThan(0));
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
   * Validates if a string is valid Base64
   *
   * @param str String to validate
   * @return true if valid Base64, false otherwise
   */
  private boolean isValidBase64(String str) {
    try {
      java.util.Base64.getDecoder().decode(str);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Stub for getUser call during passwordless init
   *
   * @param phoneNumber Phone number of the user
   * @param email Email of the user
   * @param isNewUser Whether this is a new user
   * @return StubMapping for WireMock
   */
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

  /**
   * Creates device metadata map for biometric requests
   *
   * @param platform Platform (ios/android)
   * @param deviceId Unique device identifier
   * @return Device metadata map
   */
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

  /**
   * Generates a random phone number for testing
   *
   * @return Random phone number string
   */
  private String generateRandomPhoneNumber() {
    return "9" + String.format("%09d", (int) (Math.random() * 1000000000));
  }

  /**
   * Generates a random email for testing
   *
   * @return Random email string
   */
  private String generateRandomEmail() {
    return "test" + System.currentTimeMillis() + "@example.com";
  }

  /**
   * Generates a random device ID for testing
   *
   * @return Random device ID string
   */
  private String generateRandomDeviceId() {
    return "device_" + RandomStringUtils.randomAlphanumeric(16);
  }

  /**
   * Generates a random credential ID for testing
   *
   * @return Random credential ID string
   */
  private String generateRandomCredentialId() {
    return "cred_" + RandomStringUtils.randomAlphanumeric(32);
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
}
