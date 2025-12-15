package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createAuthCodeConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteAuthCodeConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getAuthCodeConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateAuthCodeConfig;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupChangelog;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthCodeConfigIT {

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
  @DisplayName("Should create auth_code_config successfully")
  public void testCreateAuthCodeConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createAuthCodeConfig(testTenantId, createAuthCodeConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getInt("ttl"), equalTo(300));
    assertThat(response.jsonPath().getInt("length"), equalTo(6));

    JsonObject dbConfig = DbUtils.getAuthCodeConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString("tenant_id"), equalTo(testTenantId));
    assertThat(dbConfig.getInteger("ttl"), equalTo(300));
    assertThat(dbConfig.getInteger("length"), equalTo(6));
  }

  @Test
  @DisplayName("Should return error when tenant_id is blank")
  public void testCreateAuthCodeConfigBlankTenantId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    requestBody.put("tenant_id", "");

    Response response = createAuthCodeConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("tenant_id cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when tenant_id exceeds 10 characters")
  public void testCreateAuthCodeConfigTenantIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    requestBody.put("tenant_id", RandomStringUtils.randomAlphanumeric(11));

    Response response = createAuthCodeConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant_id cannot exceed 10 characters"));
  }

  @Test
  @DisplayName("Should return error when ttl is null")
  public void testCreateAuthCodeConfigNullTtl() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    requestBody.put("ttl", null);

    Response response = createAuthCodeConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("ttl cannot be null"));
  }

  @Test
  @DisplayName("Should return error when ttl is less than 1")
  public void testCreateAuthCodeConfigTtlTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    requestBody.put("ttl", 0);

    Response response = createAuthCodeConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("ttl must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when length is null")
  public void testCreateAuthCodeConfigNullLength() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    requestBody.put("length", null);

    Response response = createAuthCodeConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("length cannot be null"));
  }

  @Test
  @DisplayName("Should return error when length is less than 1")
  public void testCreateAuthCodeConfigLengthTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    requestBody.put("length", 0);

    Response response = createAuthCodeConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("length must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when tenant-id header doesn't match body tenant_id")
  public void testCreateAuthCodeConfigHeaderMismatch() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    String differentTenantId = "diff" + RandomStringUtils.randomAlphanumeric(6);
    requestBody.put("tenant_id", differentTenantId);

    Response response = createAuthCodeConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant-id header must match tenant_id in request body"));
  }

  @Test
  @DisplayName("Should return error when auth_code_config already exists")
  public void testCreateAuthCodeConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Response response = createAuthCodeConfig(testTenantId, createAuthCodeConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo("auth_code_config_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("Auth code config already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should get auth_code_config successfully")
  public void testGetAuthCodeConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Response response = getAuthCodeConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getInt("ttl"), equalTo(300));
    assertThat(response.jsonPath().getInt("length"), equalTo(6));
  }

  @Test
  @DisplayName("Should return 404 when auth_code_config not found")
  public void testGetAuthCodeConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getAuthCodeConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("auth_code_config_not_found"));
  }

  @Test
  @DisplayName("Should update auth_code_config successfully with single field")
  public void testUpdateAuthCodeConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("ttl", 600);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("ttl"), equalTo(600));
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getAuthCodeConfig(testTenantId);
    assertThat(dbConfig.getInteger("ttl"), equalTo(600));
  }

  @Test
  @DisplayName("Should update auth_code_config successfully with multiple fields")
  public void testUpdateAuthCodeConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("ttl", 600);
    updateBody.put("length", 8);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("ttl"), equalTo(600));
    assertThat(response.jsonPath().getInt("length"), equalTo(8));

    JsonObject dbConfig = DbUtils.getAuthCodeConfig(testTenantId);
    assertThat(dbConfig.getInteger("ttl"), equalTo(600));
    assertThat(dbConfig.getInteger("length"), equalTo(8));
  }

  @Test
  @DisplayName("Should update auth_code_config partially - only provided fields")
  public void testUpdateAuthCodeConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("length", 8);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("length"), equalTo(8));
    assertThat(response.jsonPath().getInt("ttl"), equalTo(300));
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateAuthCodeConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("no_fields_to_update"));
  }

  @Test
  @DisplayName("Should return error when ttl is less than 1 in update")
  public void testUpdateAuthCodeConfigTtlTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("ttl", 0);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("ttl must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when length is less than 1 in update")
  public void testUpdateAuthCodeConfigLengthTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("length", 0);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("length must be greater than 0"));
  }

  @Test
  @DisplayName("Should return 404 when auth_code_config not found for update")
  public void testUpdateAuthCodeConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("ttl", 600);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("auth_code_config_not_found"));
  }

  @Test
  @DisplayName("Should delete auth_code_config successfully")
  public void testDeleteAuthCodeConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Response response = deleteAuthCodeConfig(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getAuthCodeConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 404 when auth_code_config not found for delete")
  public void testDeleteAuthCodeConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteAuthCodeConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("auth_code_config_not_found"));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put("id", testTenantId);
    tenantBody.put("name", testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createAuthCodeConfigBody() {
    Map<String, Object> authCodeConfigBody = new HashMap<>();
    authCodeConfigBody.put("tenant_id", testTenantId);
    authCodeConfigBody.put("ttl", 300);
    authCodeConfigBody.put("length", 6);
    return authCodeConfigBody;
  }
}
