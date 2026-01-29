package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.AUTH_RESPONSE_TYPE_TOKEN;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_LOCATION;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.HEADER_CONTENT_TYPE;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.OIDC_BODY_PARAM_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_BODY_PARAM_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.Constants.SCOPE_PROFILE;
import static com.dreamsportslabs.guardian.Constants.SOURCE_VALUE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_2;
import static com.dreamsportslabs.guardian.Constants.TEST_SCOPES_OPENID_PROFILE;
import static com.dreamsportslabs.guardian.Constants.TOTAL_COUNT;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_CREDENTIAL_TYPE_PIN;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_EMAIL_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_PASSWORD_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_PHONE_1;
import static com.dreamsportslabs.guardian.Constants.V2_SIGNIN_TEST_USERNAME_1;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getUserRefreshTokens;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.v2SignIn;
import static com.dreamsportslabs.guardian.utils.DbUtils.addDefaultClientScopes;
import static com.dreamsportslabs.guardian.utils.DbUtils.addFirstPartyClient;
import static com.dreamsportslabs.guardian.utils.DbUtils.addScope;
import static com.dreamsportslabs.guardian.utils.DbUtils.addThirdPartyClient;
import static com.dreamsportslabs.guardian.utils.DbUtils.insertOidcRefreshToken;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import com.dreamsportslabs.guardian.utils.DbUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class V2UserRefreshTokensIT {

  private static final String TENANT_ID = TENANT_1;
  private static final String TEST_SCOPE = SCOPE_PROFILE;
  private static final String TEST_USER_ID = "test-user-123";
  private static final String TEST_USER_ID_2 = "test-user-456";
  private static final String TEST_DEVICE_NAME_1 = "iPhone 13";
  private static final String TEST_DEVICE_NAME_2 = "MacBook Pro";
  private static final String TEST_DEVICE_NAME_3 = "Android Phone";
  private static final String TEST_LOCATION_1 = "New York, USA";
  private static final String TEST_LOCATION_2 = "San Francisco, USA";
  private static final String TEST_IP_1 = "192.168.1.1";
  private static final String TEST_IP_2 = "10.0.0.1";
  private static final String TEST_SOURCE_1 = "mobile";
  private static final String TEST_SOURCE_2 = "web";
  private static final String TEST_AUTH_METHOD = "[\"PASSWORD\"]";

  private static String firstPartyClientId;
  private static String secondClientId;
  private WireMockServer wireMockServer;

  @BeforeAll
  static void setup() {
    addScope(TENANT_ID, TEST_SCOPE);
    firstPartyClientId = addFirstPartyClient(TENANT_ID);
    secondClientId = addThirdPartyClient(TENANT_ID);
    addDefaultClientScopes(TENANT_ID, firstPartyClientId, TEST_SCOPE);
    addDefaultClientScopes(TENANT_ID, secondClientId, TEST_SCOPE);
  }

  @BeforeEach
  void setUp() {
    // Clean up refresh tokens before each test
    DbUtils.cleanupRefreshTokens(TENANT_ID, TEST_USER_ID);
    DbUtils.cleanupRefreshTokens(TENANT_ID, TEST_USER_ID_2);
  }

  private StubMapping stubAuthenticateUserSuccess(
      String username, String email, String phoneNumber, String credentialType, String userId) {
    assertThat("WireMockServer must be initialized", wireMockServer, notNullValue());
    io.vertx.core.json.JsonObject responseBody =
        new io.vertx.core.json.JsonObject()
            .put("email", email)
            .put("phoneNumber", phoneNumber)
            .put("username", username)
            .put("isPasswordSet", V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD.equals(credentialType))
            .put("isPinSet", V2_SIGNIN_CREDENTIAL_TYPE_PIN.equals(credentialType))
            .put("userId", userId);

    return wireMockServer.stubFor(
        WireMock.post(WireMock.urlPathMatching("/authenticateUser"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(SC_OK)
                    .withHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                    .withBody(responseBody.encode())));
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getRefreshTokensList(Response response) {
    return (List<Map<String, Object>>)
        (Object) response.jsonPath().getList("refresh_tokens", Map.class);
  }

  private String generateAccessToken(String tenantId, String userId, String clientId) {
    if (wireMockServer == null) {
      log.error("WireMockServer is not initialized");
      return null;
    }

    StubMapping stub =
        stubAuthenticateUserSuccess(
            V2_SIGNIN_TEST_USERNAME_1,
            V2_SIGNIN_TEST_EMAIL_1,
            V2_SIGNIN_TEST_PHONE_1,
            V2_SIGNIN_CREDENTIAL_TYPE_PASSWORD,
            userId);

    try {
      Response response =
          v2SignIn(
              tenantId,
              V2_SIGNIN_TEST_USERNAME_1,
              null,
              null,
              V2_SIGNIN_TEST_PASSWORD_1,
              null,
              AUTH_RESPONSE_TYPE_TOKEN,
              List.of(TEST_SCOPE),
              null,
              clientId);

      if (response.getStatusCode() == SC_OK) {
        String accessToken = response.jsonPath().getString(RESPONSE_BODY_PARAM_ACCESS_TOKEN);
        // Clean up the refresh token created by v2SignIn to avoid interfering with tests
        String refreshToken = response.jsonPath().getString(OIDC_BODY_PARAM_REFRESH_TOKEN);
        if (refreshToken != null && clientId != null) {
          try {
            DbUtils.deactivateRefreshToken(tenantId, clientId, refreshToken);
          } catch (Exception e) {
            log.warn("Failed to clean up refresh token created by generateAccessToken", e);
          }
        }
        wireMockServer.removeStub(stub);
        return accessToken;
      } else {
        log.error(
            "v2SignIn failed with status code: {}, response: {}",
            response.getStatusCode(),
            response.getBody().asString());
        wireMockServer.removeStub(stub);
        return null;
      }
    } catch (Exception e) {
      log.error("Error generating access token", e);
      wireMockServer.removeStub(stub);
      return null;
    }
  }

  @Test
  @DisplayName("Should return 400 when clientId is missing")
  public void testMissingClientId() {
    // Arrange
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    // Act - Call without client_id query param
    Response response = getUserRefreshTokens(TENANT_ID, accessToken, null);

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("client_id is required"));
  }

  @Test
  @DisplayName("Should return only refresh tokens for specific client")
  public void testGetRefreshTokensWithClientId() {
    // Arrange
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    // Insert refresh tokens for different clients
    String refreshToken1 =
        insertOidcRefreshToken(
            TENANT_ID,
            firstPartyClientId,
            TEST_USER_ID,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME_1,
            TEST_IP_1,
            TEST_SOURCE_1,
            TEST_LOCATION_1,
            TEST_AUTH_METHOD);

    String refreshToken2 =
        insertOidcRefreshToken(
            TENANT_ID,
            secondClientId,
            TEST_USER_ID,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME_2,
            TEST_IP_2,
            TEST_SOURCE_2,
            TEST_LOCATION_2,
            TEST_AUTH_METHOD);

    // Act
    Response response = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId);

    // Assert

    List<Map<String, Object>> refreshTokens = getRefreshTokensList(response);
    int totalCount = response.jsonPath().getInt(TOTAL_COUNT);

    assertThat(totalCount, equalTo(1));
    assertThat(refreshTokens, hasSize(1));
    Map<String, Object> token = refreshTokens.get(0);
    assertThat(token.get(OIDC_BODY_PARAM_REFRESH_TOKEN), equalTo(refreshToken1));
    assertThat(
        "Second client's token should not be returned",
        refreshTokens.stream().map(t -> (String) t.get(OIDC_BODY_PARAM_REFRESH_TOKEN)).toList(),
        not(hasItems(refreshToken2)));
    assertThat(token.get("device_name"), equalTo(TEST_DEVICE_NAME_1));
    assertThat(token.get(BODY_PARAM_LOCATION), equalTo(TEST_LOCATION_1));
    assertThat(token.get("ip"), equalTo(TEST_IP_1));
    assertThat(token.get(SOURCE_VALUE), equalTo(TEST_SOURCE_1));
  }

  @Test
  @DisplayName("Should return empty list when user has no active refresh tokens")
  public void testGetRefreshTokensWhenNoTokensExist() {
    // Arrange
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    // Act
    Response response = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId);

    // Assert
    response.then().statusCode(SC_OK);

    List<Map<String, Object>> refreshTokens = getRefreshTokensList(response);
    int totalCount = response.jsonPath().getInt(TOTAL_COUNT);

    assertThat(totalCount, equalTo(0));
    assertThat(refreshTokens, empty());
  }

  @Test
  @DisplayName("Should not return expired refresh tokens")
  public void testExpiredRefreshTokensNotReturned() {
    // Arrange
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    // Insert expired refresh token
    insertOidcRefreshToken(
        TENANT_ID,
        firstPartyClientId,
        TEST_USER_ID,
        -1800L, // Expired
        TEST_SCOPES_OPENID_PROFILE,
        TEST_DEVICE_NAME_1,
        TEST_IP_1,
        TEST_SOURCE_1,
        TEST_LOCATION_1,
        TEST_AUTH_METHOD);

    // Insert active refresh token
    String activeRefreshToken =
        insertOidcRefreshToken(
            TENANT_ID,
            firstPartyClientId,
            TEST_USER_ID,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME_2,
            TEST_IP_2,
            TEST_SOURCE_2,
            TEST_LOCATION_2,
            TEST_AUTH_METHOD);

    // Act
    Response response = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId);

    // Assert
    response.then().statusCode(SC_OK);

    List<Map<String, Object>> refreshTokens = getRefreshTokensList(response);
    int totalCount = response.jsonPath().getInt(TOTAL_COUNT);

    assertThat(totalCount, equalTo(1));
    assertThat(refreshTokens, hasSize(1));
    Map<String, Object> token = refreshTokens.get(0);
    assertThat(token.get(OIDC_BODY_PARAM_REFRESH_TOKEN), equalTo(activeRefreshToken));
  }

  @Test
  @DisplayName("Should not return inactive refresh tokens")
  public void testInactiveRefreshTokensNotReturned() {
    // Arrange
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    // Insert active refresh token
    String inactiveRefreshToken =
        insertOidcRefreshToken(
            TENANT_ID,
            firstPartyClientId,
            TEST_USER_ID,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME_1,
            TEST_IP_1,
            TEST_SOURCE_1,
            TEST_LOCATION_1,
            TEST_AUTH_METHOD);
    assertThat("Failed to insert inactive refresh token", inactiveRefreshToken, notNullValue());

    // Mark refresh token as inactive
    DbUtils.deactivateRefreshToken(TENANT_ID, firstPartyClientId, inactiveRefreshToken);

    // Insert active refresh token
    String activeRefreshToken =
        insertOidcRefreshToken(
            TENANT_ID,
            firstPartyClientId,
            TEST_USER_ID,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME_2,
            TEST_IP_2,
            TEST_SOURCE_2,
            TEST_LOCATION_2,
            TEST_AUTH_METHOD);
    assertThat("Failed to insert active refresh token", activeRefreshToken, notNullValue());

    // Act
    Response response = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId);

    // Assert
    response.then().statusCode(SC_OK);

    List<Map<String, Object>> refreshTokens = getRefreshTokensList(response);
    int totalCount = response.jsonPath().getInt(TOTAL_COUNT);

    // Verify only one token is returned
    assertThat(totalCount, greaterThanOrEqualTo(1));
    assertThat(refreshTokens.size(), greaterThanOrEqualTo(1));

    // Verify inactive token is not in the results
    List<String> returnedRefreshTokenValues =
        refreshTokens.stream()
            .map(token -> (String) token.get(OIDC_BODY_PARAM_REFRESH_TOKEN))
            .toList();
    assertThat(
        "Inactive refresh token should not be returned",
        returnedRefreshTokenValues,
        not(hasItems(inactiveRefreshToken)));

    // Verify active token is in the results
    assertThat(
        "Active refresh token should be returned",
        returnedRefreshTokenValues,
        hasItems(activeRefreshToken));
  }

  @Test
  @DisplayName("Should return 401 for invalid access token")
  public void testInvalidAccessToken() {
    // Arrange
    String invalidAccessToken = RandomStringUtils.randomAlphanumeric(10);

    // Act
    Response response = getUserRefreshTokens(TENANT_ID, invalidAccessToken, firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .header("WWW-Authenticate", containsString("error=\"invalid_token\""))
        .header("WWW-Authenticate", containsString("error_description=\"Invalid token\""));
  }

  @Test
  @DisplayName("Should return 401 for empty authorization header")
  public void testMissingAuthorizationHeader() {
    // Act
    Response response = getUserRefreshTokens(TENANT_ID, "", firstPartyClientId);

    // Assert
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 401 for expired access token")
  public void testExpiredAccessToken() {
    // Arrange - Using a hardcoded expired token
    String expiredAccessToken =
        "eyJhbGciOiJSUzI1NiIsImtpZCI6InRlc3Qta2lkIiwidHlwIjoiYXQrand0In0.eyJzdWIiOiIxIiwiZXhwIjoxNzUzMjU4OTkwLCJpYXQiOjE3NTMyNTg5ODksImlzcyI6Imh0dHBzOi8vdGVzdC5jb20iLCJhdWQiOiJodHRwczovL2FwaS5leGFtcGxlLmNvbSIsImNsaWVudF9pZCI6ImFiYzEyMy1jbGllbnQiLCJqdGkiOiI2ZmE1ZWFiMC0yYTAxLTRhNjEtYmYwYi1iNWQ4YWFkYzdjNDQiLCJzY29wZSI6ImVtYWlsIHBob25lIG9wZW5pZCJ9.JN6OhV8jyEW121GyOzioNm3OiHV9u8krTfOjtEMLjudFL01NFuMuDOM8NrQ-DJyzZEdwBEqxq_0oUr49yeKjVt1qY32HBykkM0Ks6G99JDLZRAQfOsz1Btx1j3EcdxnEPdyTMTWMULZmWSJrrbcttj73I5WBV8xOmt9iHOQNQEsHjfpStkMO9_y8_kx6hYcF9lKWsOb72GyQu_AoopcNmf9-JvuRE3fP7mDt1QsZegnsRlpp2WgljTMXkUBE53ccFG9ps6Hh2R0hu9V_smYwIX0_xGj7z21JdrNheH1pd1sxP0nVLwnbh_6C8JMWMxlNilgZlk5BYPTx-tuToCeD9w";

    // Act
    Response response = getUserRefreshTokens(TENANT_ID, expiredAccessToken, firstPartyClientId);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .header("WWW-Authenticate", containsString("error=\"invalid_token\""))
        .header("WWW-Authenticate", containsString("error_description=\"Token has expired\""));
  }

  @Test
  @DisplayName("Should return 401 when clientId does not match with clientId in token")
  public void testGetRefreshTokensForSecondClient() {
    // Arrange
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    // Insert refresh tokens for different clients
    insertOidcRefreshToken(
        TENANT_ID,
        firstPartyClientId,
        TEST_USER_ID,
        1800L,
        TEST_SCOPES_OPENID_PROFILE,
        TEST_DEVICE_NAME_1,
        TEST_IP_1,
        TEST_SOURCE_1,
        TEST_LOCATION_1,
        TEST_AUTH_METHOD);

    insertOidcRefreshToken(
        TENANT_ID,
        secondClientId,
        TEST_USER_ID,
        1800L,
        TEST_SCOPES_OPENID_PROFILE,
        TEST_DEVICE_NAME_2,
        TEST_IP_2,
        TEST_SOURCE_2,
        TEST_LOCATION_2,
        TEST_AUTH_METHOD);

    // Act - query for secondClientId
    Response response = getUserRefreshTokens(TENANT_ID, accessToken, secondClientId);

    // Assert
    response
        .then()
        .statusCode(SC_UNAUTHORIZED)
        .header("WWW-Authenticate", containsString("error=\"invalid_token\""))
        .header(
            "WWW-Authenticate",
            containsString("error_description=\"Invalid token: client_id mismatch\""));
  }

  @Test
  @DisplayName("Should return multiple refresh tokens with different device information")
  public void testMultipleRefreshTokensWithDifferentDevices() {
    // Arrange
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    // Insert multiple refresh tokens with different device info
    String refreshToken1 =
        insertOidcRefreshToken(
            TENANT_ID,
            firstPartyClientId,
            TEST_USER_ID,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME_1,
            TEST_IP_1,
            TEST_SOURCE_1,
            TEST_LOCATION_1,
            TEST_AUTH_METHOD);

    String refreshToken2 =
        insertOidcRefreshToken(
            TENANT_ID,
            firstPartyClientId,
            TEST_USER_ID,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME_2,
            TEST_IP_2,
            TEST_SOURCE_2,
            TEST_LOCATION_2,
            TEST_AUTH_METHOD);

    String refreshToken3 =
        insertOidcRefreshToken(
            TENANT_ID,
            firstPartyClientId,
            TEST_USER_ID,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME_3,
            TEST_IP_1,
            TEST_SOURCE_1,
            TEST_LOCATION_1,
            TEST_AUTH_METHOD);

    // Act
    Response response = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId);

    // Assert
    response.then().statusCode(SC_OK);

    List<Map<String, Object>> refreshTokens = getRefreshTokensList(response);
    int totalCount = response.jsonPath().getInt(TOTAL_COUNT);

    assertThat(totalCount, equalTo(3));
    assertThat(refreshTokens, hasSize(3));

    // Verify all tokens are present
    List<String> returnedRefreshTokens =
        refreshTokens.stream().map(token -> (String) token.get("refresh_token")).toList();
    assertThat(returnedRefreshTokens, hasItems(refreshToken1, refreshToken2, refreshToken3));

    // Verify device information for each token
    Map<String, Object> token1 =
        refreshTokens.stream()
            .filter(t -> refreshToken1.equals(t.get(OIDC_BODY_PARAM_REFRESH_TOKEN)))
            .findFirst()
            .orElse(null);
    assertThat(token1, notNullValue());
    assertThat(token1.get("device_name"), equalTo(TEST_DEVICE_NAME_1));
    assertThat(token1.get("ip"), equalTo(TEST_IP_1));
    assertThat(token1.get(SOURCE_VALUE), equalTo(TEST_SOURCE_1));
    assertThat(token1.get(BODY_PARAM_LOCATION), equalTo(TEST_LOCATION_1));

    Map<String, Object> token2 =
        refreshTokens.stream()
            .filter(t -> refreshToken2.equals(t.get(OIDC_BODY_PARAM_REFRESH_TOKEN)))
            .findFirst()
            .orElse(null);
    assertThat(token2, notNullValue());
    assertThat(token2.get("device_name"), equalTo(TEST_DEVICE_NAME_2));
    assertThat(token2.get("ip"), equalTo(TEST_IP_2));
    assertThat(token2.get(SOURCE_VALUE), equalTo(TEST_SOURCE_2));
    assertThat(token2.get(BODY_PARAM_LOCATION), equalTo(TEST_LOCATION_2));
  }

  @Test
  @DisplayName("Should only return tokens for the user from access token")
  public void testTokensForDifferentUserNotReturned() {
    // Arrange
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    // Insert refresh token for the user from access token
    String userRefreshToken =
        insertOidcRefreshToken(
            TENANT_ID,
            firstPartyClientId,
            TEST_USER_ID,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME_1,
            TEST_IP_1,
            TEST_SOURCE_1,
            TEST_LOCATION_1,
            TEST_AUTH_METHOD);
    assertThat("Failed to insert user refresh token", userRefreshToken, notNullValue());

    // Insert refresh token for different user
    String differentUserRefreshToken =
        insertOidcRefreshToken(
            TENANT_ID,
            firstPartyClientId,
            TEST_USER_ID_2,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME_2,
            TEST_IP_2,
            TEST_SOURCE_2,
            TEST_LOCATION_2,
            TEST_AUTH_METHOD);
    assertThat(
        "Failed to insert different user refresh token", differentUserRefreshToken, notNullValue());

    // Act
    Response response = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId);

    // Assert
    response.then().statusCode(SC_OK);

    List<Map<String, Object>> refreshTokens = getRefreshTokensList(response);
    int totalCount = response.jsonPath().getInt(TOTAL_COUNT);

    // Verify only one token is returned
    assertThat(totalCount, equalTo(1));
    assertThat(refreshTokens.size(), equalTo(1));

    // Verify the user's token is in the results
    List<String> returnedRefreshTokenValues =
        refreshTokens.stream()
            .map(token -> (String) token.get(OIDC_BODY_PARAM_REFRESH_TOKEN))
            .toList();

    assertThat(
        "User's refresh token should be returned",
        returnedRefreshTokenValues.get(0),
        equalTo(userRefreshToken));
  }

  @Test
  @DisplayName("Should only return tokens for the tenant from access token")
  public void testTokensForDifferentTenantNotReturned() {
    // Arrange
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    // Insert refresh token for the tenant from access token
    String userRefreshToken =
        insertOidcRefreshToken(
            TENANT_ID,
            firstPartyClientId,
            TEST_USER_ID,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME_1,
            TEST_IP_1,
            TEST_SOURCE_1,
            TEST_LOCATION_1,
            TEST_AUTH_METHOD);

    // Insert refresh token for different tenant (should not be returned)
    insertOidcRefreshToken(
        TENANT_2,
        firstPartyClientId,
        TEST_USER_ID,
        1800L,
        TEST_SCOPES_OPENID_PROFILE,
        TEST_DEVICE_NAME_2,
        TEST_IP_2,
        TEST_SOURCE_2,
        TEST_LOCATION_2,
        TEST_AUTH_METHOD);

    // Act
    Response response = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId);

    // Assert
    response.then().statusCode(SC_OK);

    List<Map<String, Object>> refreshTokens = getRefreshTokensList(response);
    int totalCount = response.jsonPath().getInt(TOTAL_COUNT);

    assertThat(totalCount, equalTo(1));
    assertThat(refreshTokens, hasSize(1));
    assertThat(refreshTokens.get(0).get(OIDC_BODY_PARAM_REFRESH_TOKEN), equalTo(userRefreshToken));
  }

  @Test
  @DisplayName("Should return 400 when clientId is empty string")
  public void testEmptyStringClientId() {
    // Arrange
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    // Act
    Response response = getUserRefreshTokens(TENANT_ID, accessToken, "");

    // Assert
    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("client_id is required"));
  }

  @Test
  @DisplayName("Should return paginated results with total count")
  public void testPaginationReturnsCorrectPageAndTotalCount() {
    // Arrange - insert 5 refresh tokens
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    for (int i = 0; i < 5; i++) {
      insertOidcRefreshToken(
          TENANT_ID,
          firstPartyClientId,
          TEST_USER_ID,
          1800L,
          TEST_SCOPES_OPENID_PROFILE,
          "Device " + i,
          TEST_IP_1,
          TEST_SOURCE_1,
          TEST_LOCATION_1,
          TEST_AUTH_METHOD);
    }

    // Act & Assert - page 1, page_size 2
    Response page1 = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId, 1, 2);
    page1.then().statusCode(SC_OK);
    assertThat(page1.jsonPath().getLong(TOTAL_COUNT), equalTo(5L));
    assertThat(getRefreshTokensList(page1), hasSize(2));

    // Act & Assert - page 2, page_size 2
    Response page2 = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId, 2, 2);
    page2.then().statusCode(SC_OK);
    assertThat(page2.jsonPath().getLong(TOTAL_COUNT), equalTo(5L));
    assertThat(getRefreshTokensList(page2), hasSize(2));

    // Act & Assert - page 3, page_size 2 (last page has 1 item)
    Response page3 = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId, 3, 2);
    page3.then().statusCode(SC_OK);
    assertThat(page3.jsonPath().getLong(TOTAL_COUNT), equalTo(5L));
    assertThat(getRefreshTokensList(page3), hasSize(1));
  }

  @Test
  @DisplayName("Should return 400 when page is less than 1")
  public void testPageLessThanOneReturns400() {
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    Response response = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId, 0, 10);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("page value cannot be less than 1"));
  }

  @Test
  @DisplayName("Should return 400 when page_size is out of range")
  public void testPageSizeOutOfRangeReturns400() {
    String accessToken = generateAccessToken(TENANT_ID, TEST_USER_ID, firstPartyClientId);
    assertThat(accessToken, notNullValue());

    Response response = getUserRefreshTokens(TENANT_ID, accessToken, firstPartyClientId, 1, 101);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_INVALID_REQUEST))
        .body(MESSAGE, equalTo("page_size must be between 1 and 100"));
  }
}
