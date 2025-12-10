package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getTokenConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateTokenConfig;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupChangelog;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TokenConfigIT {

  private String testTenantId;
  private String testTenantName;

  @BeforeEach
  void setUp() {
    testTenantId = "test" + RandomStringUtils.randomAlphanumeric(6);
    testTenantName = "Test Tenant " + RandomStringUtils.randomAlphanumeric(4);
    cleanupChangelog(testTenantId);
    DbUtils.deleteTenant(testTenantId);
  }

  @AfterEach
  void tearDown() {
    cleanupChangelog(testTenantId);
    DbUtils.deleteTenant(testTenantId);
  }

  @Test
  @DisplayName("Should get token_config successfully")
  public void testGetTokenConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getTokenConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("algorithm"), equalTo("RS512"));
    assertThat(response.jsonPath().getString("issuer"), equalTo("https://dream11.local"));
    assertThat(response.jsonPath().getInt("access_token_expiry"), equalTo(900));
    assertThat(response.jsonPath().getInt("refresh_token_expiry"), equalTo(2592000));
    assertThat(response.jsonPath().getInt("id_token_expiry"), equalTo(36000));
    assertThat(response.jsonPath().getString("cookie_same_site"), equalTo("NONE"));
    assertThat(response.jsonPath().getString("cookie_domain"), equalTo(""));
    assertThat(response.jsonPath().getString("cookie_path"), equalTo("/"));
    assertThat(response.jsonPath().getBoolean("cookie_secure"), equalTo(false));
    assertThat(response.jsonPath().getBoolean("cookie_http_only"), equalTo(true));

    List<Object> rsaKeys = response.jsonPath().getList("rsa_keys");
    assertThat(rsaKeys.size(), equalTo(3));

    Map<String, Object> rsaKey1 = response.jsonPath().getMap("rsa_keys[0]");
    assertThat(rsaKey1.containsKey("kid"), equalTo(true));
    assertThat(rsaKey1.containsKey("public_key"), equalTo(true));
    assertThat(rsaKey1.containsKey("private_key"), equalTo(true));
    assertThat(rsaKey1.containsKey("current"), equalTo(true));
    assertThat(rsaKey1.get("current"), equalTo(true));

    Map<String, Object> rsaKey2 = response.jsonPath().getMap("rsa_keys[1]");
    assertThat(rsaKey2.containsKey("kid"), equalTo(true));
    assertThat(rsaKey2.containsKey("public_key"), equalTo(true));
    assertThat(rsaKey2.containsKey("private_key"), equalTo(true));
    assertThat(rsaKey2.containsKey("current"), equalTo(false));

    Map<String, Object> rsaKey3 = response.jsonPath().getMap("rsa_keys[2]");
    assertThat(rsaKey3.containsKey("kid"), equalTo(true));
    assertThat(rsaKey3.containsKey("public_key"), equalTo(true));
    assertThat(rsaKey3.containsKey("private_key"), equalTo(true));
    assertThat(rsaKey3.containsKey("current"), equalTo(false));

    List<String> idTokenClaims = response.jsonPath().getList("id_token_claims");
    assertThat(idTokenClaims.size(), equalTo(2));
    assertThat(idTokenClaims.contains("userId"), equalTo(true));
    assertThat(idTokenClaims.contains("emailId"), equalTo(true));

    List<String> accessTokenClaims = response.jsonPath().getList("access_token_claims");
    assertThat(accessTokenClaims.size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing")
  public void testGetTokenConfigMissingHeader() {
    Response response = given().get("/v1/admin/config/token-config");
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should update token_config successfully with single field")
  public void testUpdateTokenConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("algorithm", "RS256");

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("algorithm"), equalTo("RS256"));
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));

    List<Object> rsaKeys = response.jsonPath().getList("rsa_keys");
    assertThat(rsaKeys.size(), equalTo(3));

    Map<String, Object> rsaKey1 = response.jsonPath().getMap("rsa_keys[0]");
    assertThat(rsaKey1.containsKey("kid"), equalTo(true));
    assertThat(rsaKey1.containsKey("public_key"), equalTo(true));
    assertThat(rsaKey1.containsKey("private_key"), equalTo(true));
    assertThat(rsaKey1.containsKey("current"), equalTo(true));
    assertThat(rsaKey1.get("current"), equalTo(true));

    Map<String, Object> rsaKey2 = response.jsonPath().getMap("rsa_keys[1]");
    assertThat(rsaKey2.containsKey("kid"), equalTo(true));
    assertThat(rsaKey2.containsKey("public_key"), equalTo(true));
    assertThat(rsaKey2.containsKey("private_key"), equalTo(true));
    assertThat(rsaKey2.containsKey("current"), equalTo(false));

    Map<String, Object> rsaKey3 = response.jsonPath().getMap("rsa_keys[2]");
    assertThat(rsaKey3.containsKey("kid"), equalTo(true));
    assertThat(rsaKey3.containsKey("public_key"), equalTo(true));
    assertThat(rsaKey3.containsKey("private_key"), equalTo(true));
    assertThat(rsaKey3.containsKey("current"), equalTo(false));

    JsonObject dbConfig = DbUtils.getTokenConfig(testTenantId);
    assertThat(dbConfig.getString("algorithm"), equalTo("RS256"));
  }

  @Test
  @DisplayName("Should update token_config successfully with multiple fields")
  public void testUpdateTokenConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("algorithm", "RS256");
    updateBody.put("issuer", "https://updated.dream11.local");
    updateBody.put("access_token_expiry", 1800);
    updateBody.put("cookie_same_site", "LAX");
    updateBody.put("cookie_secure", true);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("algorithm"), equalTo("RS256"));
    assertThat(response.jsonPath().getString("issuer"), equalTo("https://updated.dream11.local"));
    assertThat(response.jsonPath().getInt("access_token_expiry"), equalTo(1800));
    assertThat(response.jsonPath().getString("cookie_same_site"), equalTo("LAX"));
    assertThat(response.jsonPath().getBoolean("cookie_secure"), equalTo(true));

    List<Object> rsaKeys = response.jsonPath().getList("rsa_keys");
    assertThat(rsaKeys.size(), equalTo(3));

    Map<String, Object> rsaKey1 = response.jsonPath().getMap("rsa_keys[0]");
    assertThat(rsaKey1.containsKey("kid"), equalTo(true));
    assertThat(rsaKey1.containsKey("public_key"), equalTo(true));
    assertThat(rsaKey1.containsKey("private_key"), equalTo(true));
    assertThat(rsaKey1.containsKey("current"), equalTo(true));
    assertThat(rsaKey1.get("current"), equalTo(true));

    Map<String, Object> rsaKey2 = response.jsonPath().getMap("rsa_keys[1]");
    assertThat(rsaKey2.containsKey("kid"), equalTo(true));
    assertThat(rsaKey2.containsKey("public_key"), equalTo(true));
    assertThat(rsaKey2.containsKey("private_key"), equalTo(true));
    assertThat(rsaKey2.containsKey("current"), equalTo(false));

    Map<String, Object> rsaKey3 = response.jsonPath().getMap("rsa_keys[2]");
    assertThat(rsaKey3.containsKey("kid"), equalTo(true));
    assertThat(rsaKey3.containsKey("public_key"), equalTo(true));
    assertThat(rsaKey3.containsKey("private_key"), equalTo(true));
    assertThat(rsaKey3.containsKey("current"), equalTo(false));

    JsonObject dbConfig = DbUtils.getTokenConfig(testTenantId);
    assertThat(dbConfig.getString("algorithm"), equalTo("RS256"));
    assertThat(dbConfig.getString("issuer"), equalTo("https://updated.dream11.local"));
    assertThat(dbConfig.getInteger("access_token_expiry"), equalTo(1800));
    assertThat(dbConfig.getString("cookie_same_site"), equalTo("LAX"));
    assertThat(dbConfig.getBoolean("cookie_secure"), equalTo(true));
  }

  @Test
  @DisplayName("Should update token_config partially - only provided fields")
  public void testUpdateTokenConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("access_token_expiry", 3600);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("access_token_expiry"), equalTo(3600));
    assertThat(response.jsonPath().getString("algorithm"), equalTo("RS512"));
    assertThat(response.jsonPath().getString("issuer"), equalTo("https://dream11.local"));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for update")
  public void testUpdateTokenConfigMissingHeader() {
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("algorithm", "RS256");

    Response response =
        given()
            .header("Content-Type", "application/json")
            .body(updateBody)
            .patch("/v1/admin/config/token-config");

    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateTokenConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateTokenConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("no_fields_to_update"));
  }

  @Test
  @DisplayName("Should return error when algorithm is blank")
  public void testUpdateTokenConfigBlankAlgorithm() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("algorithm", "");

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("algorithm cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when algorithm exceeds 10 characters")
  public void testUpdateTokenConfigAlgorithmTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longAlgorithm = RandomStringUtils.randomAlphanumeric(11);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("algorithm", longAlgorithm);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("algorithm cannot exceed 10 characters"));
  }

  @Test
  @DisplayName("Should return error when issuer is blank")
  public void testUpdateTokenConfigBlankIssuer() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("issuer", "");

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("issuer cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when issuer exceeds 256 characters")
  public void testUpdateTokenConfigIssuerTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longIssuer = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("issuer", longIssuer);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("issuer cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when access_token_expiry is less than 1")
  public void testUpdateTokenConfigAccessTokenExpiryTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("access_token_expiry", 0);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("access_token_expiry must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when refresh_token_expiry is less than 1")
  public void testUpdateTokenConfigRefreshTokenExpiryTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("refresh_token_expiry", 0);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("refresh_token_expiry must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when id_token_expiry is less than 1")
  public void testUpdateTokenConfigIdTokenExpiryTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("id_token_expiry", 0);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("id_token_expiry must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when cookie_same_site is blank")
  public void testUpdateTokenConfigBlankCookieSameSite() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("cookie_same_site", "");

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("cookie_same_site cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when cookie_same_site exceeds 20 characters")
  public void testUpdateTokenConfigCookieSameSiteTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longCookieSameSite = RandomStringUtils.randomAlphanumeric(21);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("cookie_same_site", longCookieSameSite);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("cookie_same_site cannot exceed 20 characters"));
  }

  @Test
  @DisplayName("Should return error when cookie_domain exceeds 256 characters")
  public void testUpdateTokenConfigCookieDomainTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longCookieDomain = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("cookie_domain", longCookieDomain);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("cookie_domain cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when cookie_path is blank")
  public void testUpdateTokenConfigBlankCookiePath() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("cookie_path", "");

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("cookie_path cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when cookie_path exceeds 256 characters")
  public void testUpdateTokenConfigCookiePathTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longCookiePath = "/" + RandomStringUtils.randomAlphanumeric(256);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("cookie_path", longCookiePath);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("cookie_path cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should update id_token_claims successfully")
  public void testUpdateTokenConfigIdTokenClaims() {
    createTenant(createTenantBody()).then().statusCode(201);

    List<String> newClaims = new ArrayList<>();
    newClaims.add("userId");
    newClaims.add("emailId");
    newClaims.add("phoneNumber");

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("id_token_claims", newClaims);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    List<String> idTokenClaims = response.jsonPath().getList("id_token_claims");
    assertThat(idTokenClaims.size(), equalTo(3));
    assertThat(idTokenClaims.contains("userId"), equalTo(true));
    assertThat(idTokenClaims.contains("emailId"), equalTo(true));
    assertThat(idTokenClaims.contains("phoneNumber"), equalTo(true));
  }

  @Test
  @DisplayName("Should update access_token_claims successfully")
  public void testUpdateTokenConfigAccessTokenClaims() {
    createTenant(createTenantBody()).then().statusCode(201);

    List<String> newClaims = new ArrayList<>();
    newClaims.add("userId");
    newClaims.add("emailId");

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("access_token_claims", newClaims);

    Response response = updateTokenConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    List<String> accessTokenClaims = response.jsonPath().getList("access_token_claims");
    assertThat(accessTokenClaims.size(), equalTo(2));
    assertThat(accessTokenClaims.contains("userId"), equalTo(true));
    assertThat(accessTokenClaims.contains("emailId"), equalTo(true));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put("id", testTenantId);
    tenantBody.put("name", testTenantName);
    return tenantBody;
  }
}
