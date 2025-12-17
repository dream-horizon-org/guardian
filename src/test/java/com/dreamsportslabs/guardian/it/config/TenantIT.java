package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_TENANT_NAME_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_TENANT_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_ID_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_NAME_CANNOT_EXCEED_256;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_NAME_REQUIRED;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_TENANT_NAME_ALREADY_EXISTS_PREFIX;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_NAME;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_ACCESS_TOKEN_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_ID_TOKEN_CLAIMS;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_RSA_KEYS;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getTenantByName;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateTenant;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupChangelog;
import static com.dreamsportslabs.guardian.utils.DbUtils.getTokenConfig;
import static com.dreamsportslabs.guardian.utils.DbUtils.getUserConfig;
import static com.dreamsportslabs.guardian.utils.DbUtils.tenantExists;
import static com.dreamsportslabs.guardian.utils.DbUtils.tokenConfigExists;
import static com.dreamsportslabs.guardian.utils.DbUtils.userConfigExists;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TenantIT {

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
  @DisplayName("Should create tenant successfully")
  public void testCreateTenantSuccess() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString("id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("name"), equalTo(testTenantName));
    assertThat(tenantExists(testTenantId), equalTo(true));
  }

  @Test
  @DisplayName("Should create default user_config when tenant is created")
  public void testCreateTenantWithDefaultUserConfig() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(userConfigExists(testTenantId), equalTo(true));

    JsonObject userConfig = getUserConfig(testTenantId);
    assertThat(userConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(userConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(userConfig.getBoolean("is_ssl_enabled"), equalTo(false));
    assertThat(userConfig.getString("host"), equalTo("control-tower.dream11.local"));
    assertThat(userConfig.getInteger("port"), equalTo(80));
    assertThat(userConfig.getString("get_user_path"), equalTo("/users/validate"));
    assertThat(userConfig.getString("create_user_path"), equalTo("/users"));
    assertThat(userConfig.getString("authenticate_user_path"), equalTo("/api/user/validate"));
    assertThat(userConfig.getString("add_provider_path"), equalTo(""));
    assertThat(userConfig.getBoolean("send_provider_details"), equalTo(false));
  }

  @Test
  @DisplayName("Should create default token_config when tenant is created")
  public void testCreateTenantWithDefaultTokenConfig() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(tokenConfigExists(testTenantId), equalTo(true));

    JsonObject tokenConfig = getTokenConfig(testTenantId);
    assertThat(tokenConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(tokenConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(tokenConfig.getString("algorithm"), equalTo("RS512"));
    assertThat(tokenConfig.getString("issuer"), equalTo("https://dream11.local"));
    assertThat(tokenConfig.getInteger("access_token_expiry"), equalTo(900));
    assertThat(tokenConfig.getInteger("refresh_token_expiry"), equalTo(2592000));
    assertThat(tokenConfig.getInteger("id_token_expiry"), equalTo(36000));
    assertThat(tokenConfig.getString("cookie_same_site"), equalTo("NONE"));
    assertThat(tokenConfig.getString("cookie_domain"), equalTo(""));
    assertThat(tokenConfig.getString("cookie_path"), equalTo("/"));
    assertThat(tokenConfig.getBoolean("cookie_secure"), equalTo(false));
    assertThat(tokenConfig.getBoolean("cookie_http_only"), equalTo(true));

    JsonArray rsaKeys = new JsonArray(tokenConfig.getString(RESPONSE_FIELD_RSA_KEYS));
    assertThat(rsaKeys.size(), equalTo(3));

    JsonObject firstRsaKey = rsaKeys.getJsonObject(0);
    assertThat(firstRsaKey.getString("kid"), org.hamcrest.Matchers.notNullValue());
    assertThat(firstRsaKey.getString("public_key"), org.hamcrest.Matchers.notNullValue());
    assertThat(firstRsaKey.getString("private_key"), org.hamcrest.Matchers.notNullValue());
    assertThat(firstRsaKey.getBoolean("current"), equalTo(true));

    for (int i = 1; i < 3; i++) {
      JsonObject rsaKey = rsaKeys.getJsonObject(i);
      assertThat(rsaKey.getString("kid"), org.hamcrest.Matchers.notNullValue());
      assertThat(rsaKey.getString("public_key"), org.hamcrest.Matchers.notNullValue());
      assertThat(rsaKey.getString("private_key"), org.hamcrest.Matchers.notNullValue());
      assertThat(rsaKey.containsKey("current"), equalTo(false));
    }

    JsonArray idTokenClaims = new JsonArray(tokenConfig.getString(RESPONSE_FIELD_ID_TOKEN_CLAIMS));
    assertThat(idTokenClaims.size(), equalTo(2));
    assertThat(idTokenClaims.contains("userId"), equalTo(true));
    assertThat(idTokenClaims.contains("emailId"), equalTo(true));

    JsonArray accessTokenClaims =
        new JsonArray(tokenConfig.getString(RESPONSE_FIELD_ACCESS_TOKEN_CLAIMS));
    assertThat(accessTokenClaims.size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return error when id is missing")
  public void testCreateTenantMissingId() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo(ERROR_MSG_ID_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when name is missing")
  public void testCreateTenantMissingName() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", testTenantId);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo(ERROR_MSG_NAME_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when id is blank")
  public void testCreateTenantBlankId() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, "");
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo(ERROR_MSG_ID_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when name is blank")
  public void testCreateTenantBlankName() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_NAME, "");

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo(ERROR_MSG_NAME_REQUIRED));
  }

  @Test
  @DisplayName("Should return error when id exceeds 10 characters")
  public void testCreateTenantIdTooLong() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, "12345678901");
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("id cannot exceed 10 characters"));
  }

  @Test
  @DisplayName("Should return error when name exceeds 256 characters")
  public void testCreateTenantNameTooLong() {
    String longName = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_NAME, longName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_NAME_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when tenant id already exists")
  public void testCreateTenantDuplicateId() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    createTenant(requestBody).then().statusCode(SC_CREATED);

    Map<String, Object> duplicateRequest = new HashMap<>();
    duplicateRequest.put(REQUEST_FIELD_ID, testTenantId);
    duplicateRequest.put(REQUEST_FIELD_NAME, "Another Name");

    Response response = createTenant(duplicateRequest);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("tenant_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("Tenant ID already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should return error when tenant name already exists")
  public void testCreateTenantDuplicateName() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    createTenant(requestBody).then().statusCode(SC_CREATED);

    Map<String, Object> duplicateRequest = new HashMap<>();
    duplicateRequest.put(REQUEST_FIELD_ID, "different");
    duplicateRequest.put(REQUEST_FIELD_NAME, testTenantName);

    Response response = createTenant(duplicateRequest);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_TENANT_NAME_ALREADY_EXISTS));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TENANT_NAME_ALREADY_EXISTS_PREFIX + testTenantName));
  }

  @Test
  @DisplayName("Should get tenant by id successfully")
  public void testGetTenantByIdSuccess() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    createTenant(requestBody).then().statusCode(SC_CREATED);

    Response response = getTenant(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("name"), equalTo(testTenantName));
  }

  @Test
  @DisplayName("Should return 400 when tenant not found by id")
  public void testGetTenantByIdNotFound() {
    Response response = getTenant("nonexistent");

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_TENANT_NOT_FOUND));
  }

  @Test
  @DisplayName("Should get tenant by name successfully")
  public void testGetTenantByNameSuccess() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    createTenant(requestBody).then().statusCode(SC_CREATED);

    Response response = getTenantByName(testTenantName);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("name"), equalTo(testTenantName));
  }

  @Test
  @DisplayName("Should return 400 when tenant not found by name")
  public void testGetTenantByNameNotFound() {
    Response response = getTenantByName("Nonexistent Tenant");

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_TENANT_NOT_FOUND));
  }

  @Test
  @DisplayName("Should update tenant successfully")
  public void testUpdateTenantSuccess() {
    Map<String, Object> createBody = new HashMap<>();
    createBody.put(REQUEST_FIELD_ID, testTenantId);
    createBody.put(REQUEST_FIELD_NAME, testTenantName);

    createTenant(createBody).then().statusCode(SC_CREATED);

    String updatedName = "Updated " + testTenantName;
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_NAME, updatedName);

    Response response = updateTenant(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("name"), equalTo(updatedName));

    Response getResponse = getTenant(testTenantId);
    assertThat(getResponse.jsonPath().getString("name"), equalTo(updatedName));
  }

  @Test
  @DisplayName("Should return 400 when updating non-existent tenant")
  public void testUpdateTenantNotFound() {
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_NAME, "Updated Name");

    Response response = updateTenant("nonexistent", updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_TENANT_NOT_FOUND));
  }

  @Test
  @DisplayName("Should return error when update name is blank")
  public void testUpdateTenantBlankName() {
    Map<String, Object> createBody = new HashMap<>();
    createBody.put(REQUEST_FIELD_ID, testTenantId);
    createBody.put(REQUEST_FIELD_NAME, testTenantName);

    createTenant(createBody).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_NAME, "");

    Response response = updateTenant(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("name cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when update name exceeds 256 characters")
  public void testUpdateTenantNameTooLong() {
    Map<String, Object> createBody = new HashMap<>();
    createBody.put(REQUEST_FIELD_ID, testTenantId);
    createBody.put(REQUEST_FIELD_NAME, testTenantName);

    createTenant(createBody).then().statusCode(SC_CREATED);

    String longName = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_NAME, longName);

    Response response = updateTenant(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_NAME_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when update has no fields")
  public void testUpdateTenantNoFields() {
    Map<String, Object> createBody = new HashMap<>();
    createBody.put(REQUEST_FIELD_ID, testTenantId);
    createBody.put(REQUEST_FIELD_NAME, testTenantName);

    createTenant(createBody).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateTenant(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
  }

  @Test
  @DisplayName("Should return error when updating with duplicate name")
  public void testUpdateTenantDuplicateName() {
    String tenantId2 = "test2" + RandomStringUtils.randomAlphanumeric(5);
    String tenantName2 = "Another Tenant";

    Map<String, Object> createBody1 = new HashMap<>();
    createBody1.put(REQUEST_FIELD_ID, testTenantId);
    createBody1.put(REQUEST_FIELD_NAME, testTenantName);
    createTenant(createBody1).then().statusCode(SC_CREATED);

    Map<String, Object> createBody2 = new HashMap<>();
    createBody2.put(REQUEST_FIELD_ID, tenantId2);
    createBody2.put(REQUEST_FIELD_NAME, tenantName2);
    createTenant(createBody2).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_NAME, tenantName2);

    Response response = updateTenant(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_TENANT_NAME_ALREADY_EXISTS));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TENANT_NAME_ALREADY_EXISTS_PREFIX + tenantName2));

    DbUtils.deleteTenant(tenantId2);
  }

  @Test
  @DisplayName("Should delete tenant successfully")
  public void testDeleteTenantSuccess() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    createTenant(requestBody).then().statusCode(SC_CREATED);
    assertThat(tenantExists(testTenantId), equalTo(true));

    Response response = deleteTenant(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);
    assertThat(tenantExists(testTenantId), equalTo(false));
  }

  @Test
  @DisplayName("Should return 400 when deleting non-existent tenant")
  public void testDeleteTenantNotFound() {
    Response response = deleteTenant("nonexistent");

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_TENANT_NOT_FOUND));
  }

  @Test
  @DisplayName("Should create tenant, user_config, and token_config atomically")
  public void testTransactionAtomicity() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_NAME, testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_CREATED);

    assertThat(tenantExists(testTenantId), equalTo(true));
    assertThat(userConfigExists(testTenantId), equalTo(true));
    assertThat(tokenConfigExists(testTenantId), equalTo(true));
  }

  @Test
  @DisplayName("Should create tenant, user_config, and token_config atomically")
  public void testTransactionAtomicity() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", testTenantId);
    requestBody.put("name", testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_CREATED);

    assertThat(tenantExists(testTenantId), equalTo(true));
    assertThat(userConfigExists(testTenantId), equalTo(true));
    assertThat(tokenConfigExists(testTenantId), equalTo(true));
  }
}
