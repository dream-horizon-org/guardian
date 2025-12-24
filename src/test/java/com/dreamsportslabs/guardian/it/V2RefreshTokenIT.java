package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.ADDITIONAL_CLAIM_ITEM1;
import static com.dreamsportslabs.guardian.Constants.ADDITIONAL_CLAIM_ITEM2;
import static com.dreamsportslabs.guardian.Constants.BODY_PARAM_USERID;
import static com.dreamsportslabs.guardian.Constants.CLAIM_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.CLAIM_EMAIL;
import static com.dreamsportslabs.guardian.Constants.CLAIM_PHONE_NUMBER_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static com.dreamsportslabs.guardian.Constants.EMAIL_DOMAIN_EXAMPLE;
import static com.dreamsportslabs.guardian.Constants.HEADER_CONTENT_TYPE;
import static com.dreamsportslabs.guardian.Constants.JSON_EMAIL_VERIFIED;
import static com.dreamsportslabs.guardian.Constants.JSON_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_ISS;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_RFT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_SUB;
import static com.dreamsportslabs.guardian.Constants.JWT_CLAIM_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.JWT_HEADER_KID;
import static com.dreamsportslabs.guardian.Constants.LOCATION_VALUE;
import static com.dreamsportslabs.guardian.Constants.TENANT3_PUBLIC_KEY_PATH;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.Constants.TENANT_3;
import static com.dreamsportslabs.guardian.Constants.TEST_ADDITIONAL_CLAIM_VALUE_A;
import static com.dreamsportslabs.guardian.Constants.TEST_ADDITIONAL_CLAIM_VALUE_B;
import static com.dreamsportslabs.guardian.Constants.TEST_APPLICATION_TYPE;
import static com.dreamsportslabs.guardian.Constants.TEST_AUTH_METHOD_OTP;
import static com.dreamsportslabs.guardian.Constants.TEST_AUTH_METHOD_PASSWORD;
import static com.dreamsportslabs.guardian.Constants.TEST_CITY;
import static com.dreamsportslabs.guardian.Constants.TEST_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.TEST_COMPLETELY_INVALID_TOKEN;
import static com.dreamsportslabs.guardian.Constants.TEST_DEVICE_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_FIRST_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_FIRST_NAME_VALUE;
import static com.dreamsportslabs.guardian.Constants.TEST_INVALID_REFRESH_TOKEN;
import static com.dreamsportslabs.guardian.Constants.TEST_IP_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.TEST_ISSUER;
import static com.dreamsportslabs.guardian.Constants.TEST_KID;
import static com.dreamsportslabs.guardian.Constants.TEST_LAST_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_LAST_NAME_VALUE;
import static com.dreamsportslabs.guardian.Constants.TEST_MIDDLE_NAME;
import static com.dreamsportslabs.guardian.Constants.TEST_PUBLIC_KEY_PATH;
import static com.dreamsportslabs.guardian.Constants.TEST_SAMPLE_ADDRESS;
import static com.dreamsportslabs.guardian.Constants.TEST_SCOPES_OPENID_PROFILE;
import static com.dreamsportslabs.guardian.Constants.TEST_TENANT_2;
import static com.dreamsportslabs.guardian.Constants.TEST_USER_ID_1234;
import static com.dreamsportslabs.guardian.Constants.TEST_VALUE;
import static com.dreamsportslabs.guardian.Constants.TEST_VALUE_1;
import static com.dreamsportslabs.guardian.Constants.TEST_VALUE_2;
import static com.dreamsportslabs.guardian.Constants.TEST_WRONG_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.TOKEN_TYPE_BEARER;
import static com.dreamsportslabs.guardian.Constants.WIREMOCK_USER_ENDPOINT;
import static com.dreamsportslabs.guardian.constant.Constants.ACCESS_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.constant.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.v2RefreshToken;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.ApplicationIoUtils;
import com.dreamsportslabs.guardian.utils.DbUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;
import io.restassured.response.Response;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class V2RefreshTokenIT {
  public static String tenant1 = TENANT_1;
  public static String tenant3 = TENANT_3;
  public static String clientId = TEST_CLIENT_ID;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private WireMockServer wireMockServer;

  @Test()
  @DisplayName("Should generate access token for a valid OIDC refresh token")
  public void testValidOidcRefreshToken() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("access_token", isA(String.class))
        .body("token_type", equalTo(TOKEN_TYPE_BEARER))
        .body("expires_in", isA(Integer.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    assertThat(jwt.getHeaderClaim(JWT_HEADER_KID), equalTo(TEST_KID));
    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(userId));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(JWT_CLAIM_TENANT_ID), equalTo(tenant1));
    assertThat(
        claims.get(JWT_CLAIM_RFT_ID), equalTo(DigestUtils.md5Hex(refreshToken).toUpperCase()));
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(accessToken));
  }

  @Test()
  @DisplayName("Should generate access token without client_id validation")
  public void testValidOidcRefreshTokenWithoutClientId() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, null);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));
  }

  @Test()
  @DisplayName("Should return error for invalid refresh token")
  public void testInvalidRefreshToken() {
    // Arrange
    String refreshToken = TEST_INVALID_REFRESH_TOKEN;

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookies().containsKey(REFRESH_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(""));
    assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME), equalTo(""));
  }

  @Test()
  @DisplayName("Should return error for wrong client_id")
  public void testWrongClientId() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, TEST_WRONG_CLIENT_ID);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test()
  @DisplayName("Should return error for expired refresh token")
  public void testExpiredRefreshToken() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            -1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test()
  @DisplayName("Should return error for refresh token from different tenant")
  public void testDifferentTenantRefreshToken() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            TEST_TENANT_2,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test()
  @DisplayName("Should add additional claims in Access Token if setting is enabled")
  public void testAdditionalClaimsEnabled() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    StubMapping stub = getStubForUserInfoWithAdditionalClaims(userId);
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant3,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant3, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    assertThat(claims.get(ADDITIONAL_CLAIM_ITEM1), equalTo(TEST_ADDITIONAL_CLAIM_VALUE_A));
    assertThat(claims.get(ADDITIONAL_CLAIM_ITEM2), equalTo(TEST_ADDITIONAL_CLAIM_VALUE_B));
    wireMockServer.removeStub(stub);
  }

  @Test()
  @DisplayName("Should handle missing additional claims field gracefully")
  public void testMissingAdditionalClaimsField() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    StubMapping stub = getStubForUserInfoWithoutAdditionalClaims(userId);
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant3,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant3, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    assertThat(claims.containsKey(ADDITIONAL_CLAIM_ITEM1), equalTo(false));
    assertThat(claims.containsKey(ADDITIONAL_CLAIM_ITEM2), equalTo(false));
    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(userId));
    assertThat(claims.get(JWT_CLAIM_ISS), equalTo(TEST_ISSUER));
    assertThat(claims.get(JWT_CLAIM_TENANT_ID), equalTo(tenant3));

    wireMockServer.removeStub(stub);
  }

  @Test()
  @DisplayName("Should handle auth methods in token generation")
  public void testAuthMethodsInToken() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_OTP);

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get(TEST_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(userId));
    assertThat(
        claims.get(JWT_CLAIM_RFT_ID), equalTo(DigestUtils.md5Hex(refreshToken).toUpperCase()));
  }

  @Test()
  @DisplayName("Should return error and clear cookies for invalid refresh token")
  public void testInvalidRefreshTokenErrorHandling() {
    // Arrange
    String refreshToken = TEST_COMPLETELY_INVALID_TOKEN;

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate error response
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);

    // Validate cookies are cleared
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookies().containsKey(REFRESH_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(""));
    assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME), equalTo(""));
  }

  @Test()
  @DisplayName("Should handle malformed request gracefully")
  public void testMalformedRequest() {
    // Arrange - empty refresh token
    String refreshToken = "";

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
  }

  @Test()
  @DisplayName("Should handle null client_id gracefully")
  public void testNullClientIdHandling() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act - pass null client_id
    Response response = v2RefreshToken(tenant1, refreshToken, null);

    // Validate - should work without client_id validation
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));
  }

  @Test()
  @DisplayName("Should handle expired refresh token with proper error response")
  public void testExpiredRefreshTokenErrorHandling() {
    // Arrange - create expired token
    String userId = TEST_USER_ID_1234;
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant1,
            clientId,
            userId,
            -3600L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant1, refreshToken, clientId);

    // Validate error handling
    response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);

    // Validate error cookies are set
    assertThat(response.getCookies().containsKey(ACCESS_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookies().containsKey(REFRESH_TOKEN_COOKIE_NAME), is(true));
    assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME), equalTo(""));
    assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME), equalTo(""));
  }

  private StubMapping getStubForUserInfoWithAdditionalClaims(String userId) {
    JsonNode jsonNode =
        objectMapper
            .createObjectNode()
            .put(BODY_PARAM_USERID, userId)
            .put(CLAIM_EMAIL, randomAlphanumeric(8) + EMAIL_DOMAIN_EXAMPLE)
            .put(CLAIM_ADDRESS, TEST_SAMPLE_ADDRESS)
            .put(JSON_EMAIL_VERIFIED, true)
            .put(JSON_PHONE_NUMBER, randomNumeric(10))
            .put(CLAIM_PHONE_NUMBER_VERIFIED, true)
            .put(ADDITIONAL_CLAIM_ITEM1, TEST_ADDITIONAL_CLAIM_VALUE_A)
            .put(ADDITIONAL_CLAIM_ITEM2, TEST_ADDITIONAL_CLAIM_VALUE_B);

    return wireMockServer.stubFor(
        get(urlPathMatching(WIREMOCK_USER_ENDPOINT))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                    .withJsonBody(jsonNode)));
  }

  private StubMapping getStubForUserInfoWithoutAdditionalClaims(String userId) {
    JsonNode jsonNode =
        objectMapper
            .createObjectNode()
            .put(BODY_PARAM_USERID, userId)
            .put(CLAIM_EMAIL, randomAlphanumeric(8) + EMAIL_DOMAIN_EXAMPLE)
            .put(CLAIM_ADDRESS, TEST_SAMPLE_ADDRESS)
            .put(JSON_EMAIL_VERIFIED, true)
            .put(JSON_PHONE_NUMBER, randomNumeric(10))
            .put(CLAIM_PHONE_NUMBER_VERIFIED, true);

    return wireMockServer.stubFor(
        get(urlPathMatching(WIREMOCK_USER_ENDPOINT))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                    .withJsonBody(jsonNode)));
  }

  @Test()
  @DisplayName("Should extract claim name from nested JsonPath and add to Access Token")
  public void testAdditionalClaimsWithNestedJsonPath() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    // Update token_config to use nested path
    updateAccessTokenClaims(
        tenant3, "[\"user[0].name.firstName\", \"user[0].name.lastName\",  \"item1\", \"item2\"]");
    StubMapping stub = getStubForUserInfoWithNestedStructure(userId);
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant3,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant3, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    // Verify that claim names are extracted (firstName, lastName) not full paths
    assertThat(claims.get(TEST_FIRST_NAME), equalTo(TEST_FIRST_NAME_VALUE));
    assertThat(claims.get(TEST_LAST_NAME), equalTo(TEST_LAST_NAME_VALUE));
    // Verify full paths are NOT present
    assertThat(claims.containsKey("user[0].name.firstName"), equalTo(false));
    assertThat(claims.containsKey("user[0].name.lastName"), equalTo(false));

    wireMockServer.removeStub(stub);
  }

  @Test()
  @DisplayName("Should handle array notation in JsonPath correctly")
  public void testAdditionalClaimsWithArrayNotation() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    // Update token_config to use array path
    updateAccessTokenClaims(
        tenant3, "[\"items[0].value\", \"items[1].value\", \"item1\", \"item2\"]");
    StubMapping stub = getStubForUserInfoWithArrayStructure(userId);
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant3,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant3, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    // Verify claim names are extracted correctly
    // Note: Both paths extract "value" as claim name, so last one processed wins
    assertThat(claims.get(TEST_VALUE), equalTo(TEST_VALUE_2)); // Last value wins if same claim name
    // Verify full paths are NOT present
    assertThat(claims.containsKey("items[0].value"), equalTo(false));

    wireMockServer.removeStub(stub);
  }

  @Test()
  @DisplayName("Should handle missing nested path gracefully")
  public void testAdditionalClaimsWithMissingNestedPath() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    // Update token_config to use non-existent nested path
    updateAccessTokenClaims(
        tenant3, "[\"user[0].name.middleName\", \"user[0].address.city\",  \"item1\", \"item2\"]");
    StubMapping stub = getStubForUserInfoWithNestedStructure(userId);
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant3,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant3, refreshToken, clientId);

    // Validate - should succeed without the missing claims
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    // Verify missing claims are not present
    assertThat(claims.containsKey(TEST_MIDDLE_NAME), equalTo(false));
    assertThat(claims.containsKey(TEST_CITY), equalTo(false));
    // Verify standard claims are still present
    assertThat(claims.get(JWT_CLAIM_SUB), equalTo(userId));

    wireMockServer.removeStub(stub);
  }

  @Test()
  @DisplayName("Should maintain backward compatibility with simple flat keys")
  public void testAdditionalClaimsWithSimplePathBackwardCompatibility() {
    // Arrange
    String userId = TEST_USER_ID_1234;
    // Use simple flat keys (existing behavior)
    updateAccessTokenClaims(tenant3, "[\"item1\", \"item2\"]");
    StubMapping stub = getStubForUserInfoWithAdditionalClaims(userId);
    String refreshToken =
        DbUtils.insertOidcRefreshToken(
            tenant3,
            clientId,
            userId,
            1800L,
            TEST_SCOPES_OPENID_PROFILE,
            TEST_DEVICE_NAME,
            TEST_IP_ADDRESS,
            TEST_APPLICATION_TYPE,
            LOCATION_VALUE,
            TEST_AUTH_METHOD_PASSWORD);

    // Act
    Response response = v2RefreshToken(tenant3, refreshToken, clientId);

    // Validate
    response.then().statusCode(HttpStatus.SC_OK).body("access_token", isA(String.class));

    String accessToken = response.getBody().jsonPath().getString("access_token");
    Path path = Paths.get(TENANT3_PUBLIC_KEY_PATH);

    JWT jwt = JWT.getDecoder().decode(accessToken, RSAVerifier.newVerifier(path));
    Map<String, Object> claims = jwt.getAllClaims();

    // Verify backward compatibility - simple keys work as before
    assertThat(claims.get(ADDITIONAL_CLAIM_ITEM1), equalTo(TEST_ADDITIONAL_CLAIM_VALUE_A));
    assertThat(claims.get(ADDITIONAL_CLAIM_ITEM2), equalTo(TEST_ADDITIONAL_CLAIM_VALUE_B));

    wireMockServer.removeStub(stub);
  }

  private void updateAccessTokenClaims(String tenantId, String accessTokenClaimsJson) {
    DbUtils.updateTokenConfigAccessTokenClaims(tenantId, accessTokenClaimsJson);
    // Invalidate cache via API to ensure the updated configuration is loaded on next access
    ApplicationIoUtils.clearCache(tenantId);
  }

  private StubMapping getStubForUserInfoWithNestedStructure(String userId) {
    JsonNode nameNode =
        objectMapper
            .createObjectNode()
            .put(TEST_FIRST_NAME, TEST_FIRST_NAME_VALUE)
            .put(TEST_LAST_NAME, TEST_LAST_NAME_VALUE);

    JsonNode userNode = objectMapper.createObjectNode().set("name", nameNode);

    JsonNode rootNode =
        objectMapper
            .createObjectNode()
            .put(BODY_PARAM_USERID, userId)
            .put(CLAIM_EMAIL, randomAlphanumeric(8) + EMAIL_DOMAIN_EXAMPLE)
            .put(CLAIM_ADDRESS, TEST_SAMPLE_ADDRESS)
            .put(JSON_EMAIL_VERIFIED, true)
            .put(JSON_PHONE_NUMBER, randomNumeric(10))
            .put(CLAIM_PHONE_NUMBER_VERIFIED, true)
            .put(ADDITIONAL_CLAIM_ITEM1, TEST_ADDITIONAL_CLAIM_VALUE_A)
            .put(ADDITIONAL_CLAIM_ITEM2, TEST_ADDITIONAL_CLAIM_VALUE_B)
            .set("user", objectMapper.createArrayNode().add(userNode));

    return wireMockServer.stubFor(
        get(urlPathMatching(WIREMOCK_USER_ENDPOINT))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                    .withJsonBody(rootNode)));
  }

  private StubMapping getStubForUserInfoWithArrayStructure(String userId) {
    JsonNode item1 = objectMapper.createObjectNode().put(TEST_VALUE, TEST_VALUE_1);
    JsonNode item2 = objectMapper.createObjectNode().put(TEST_VALUE, TEST_VALUE_2);

    JsonNode rootNode =
        objectMapper
            .createObjectNode()
            .put(BODY_PARAM_USERID, userId)
            .put(CLAIM_EMAIL, randomAlphanumeric(8) + EMAIL_DOMAIN_EXAMPLE)
            .put(CLAIM_ADDRESS, TEST_SAMPLE_ADDRESS)
            .put(JSON_EMAIL_VERIFIED, true)
            .put(JSON_PHONE_NUMBER, randomNumeric(10))
            .put(CLAIM_PHONE_NUMBER_VERIFIED, true)
            .put(ADDITIONAL_CLAIM_ITEM1, TEST_ADDITIONAL_CLAIM_VALUE_A)
            .put(ADDITIONAL_CLAIM_ITEM2, TEST_ADDITIONAL_CLAIM_VALUE_B)
            .set("items", objectMapper.createArrayNode().add(item1).add(item2));

    return wireMockServer.stubFor(
        get(urlPathMatching(WIREMOCK_USER_ENDPOINT))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                    .withJsonBody(rootNode)));
  }
}
