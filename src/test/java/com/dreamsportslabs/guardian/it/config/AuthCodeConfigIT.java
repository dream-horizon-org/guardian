package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_AUTH_CODE_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_AUTH_CODE_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_LENGTH_CANNOT_BE_NULL;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_LENGTH_MUST_BE_GREATER_THAN_0;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_LENGTH_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_TTL_MUST_BE_GREATER_THAN_0;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_TTL_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_LENGTH;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_NAME;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TTL;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
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
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_TTL), equalTo(300));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_LENGTH), equalTo(6));

    JsonObject dbConfig = DbUtils.getAuthCodeConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_TTL), equalTo(300));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_LENGTH), equalTo(6));
  }

  @Test
  @DisplayName("Should return error when tenant_id is blank")
  public void testCreateAuthCodeConfigBlankTenantId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    requestBody.put(REQUEST_FIELD_TENANT_ID, "");

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
    requestBody.put(REQUEST_FIELD_TENANT_ID, RandomStringUtils.randomAlphanumeric(11));

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
    requestBody.put(REQUEST_FIELD_TTL, null);

    Response response = createAuthCodeConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("ttl cannot be null"));
  }

  @Test
  @DisplayName("Should return error when ttl is less than 1")
  public void testCreateAuthCodeConfigTtlTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    requestBody.put(REQUEST_FIELD_TTL, 0);

    Response response = createAuthCodeConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TTL_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1));
  }

  @Test
  @DisplayName("Should return error when length is null")
  public void testCreateAuthCodeConfigNullLength() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    requestBody.put(REQUEST_FIELD_LENGTH, null);

    Response response = createAuthCodeConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_LENGTH_CANNOT_BE_NULL));
  }

  @Test
  @DisplayName("Should return error when length is less than 1")
  public void testCreateAuthCodeConfigLengthTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    requestBody.put(REQUEST_FIELD_LENGTH, 0);

    Response response = createAuthCodeConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_LENGTH_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1));
  }

  @Test
  @DisplayName("Should return error when tenant-id header doesn't match body tenant_id")
  public void testCreateAuthCodeConfigHeaderMismatch() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAuthCodeConfigBody();
    String differentTenantId = "diff" + RandomStringUtils.randomAlphanumeric(6);
    requestBody.put(REQUEST_FIELD_TENANT_ID, differentTenantId);

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
        equalTo(ERROR_CODE_AUTH_CODE_CONFIG_ALREADY_EXISTS));
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
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_TTL), equalTo(300));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_LENGTH), equalTo(6));
  }

  @Test
  @DisplayName("Should return 404 when auth_code_config not found")
  public void testGetAuthCodeConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getAuthCodeConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_AUTH_CODE_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should update auth_code_config successfully with single field")
  public void testUpdateAuthCodeConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_TTL, 600);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_TTL), equalTo(600));
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getAuthCodeConfig(testTenantId);
    assertThat(dbConfig.getInteger(REQUEST_FIELD_TTL), equalTo(600));
  }

  @Test
  @DisplayName("Should update auth_code_config successfully with multiple fields")
  public void testUpdateAuthCodeConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_TTL, 600);
    updateBody.put(REQUEST_FIELD_LENGTH, 8);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_TTL), equalTo(600));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_LENGTH), equalTo(8));

    JsonObject dbConfig = DbUtils.getAuthCodeConfig(testTenantId);
    assertThat(dbConfig.getInteger(REQUEST_FIELD_TTL), equalTo(600));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_LENGTH), equalTo(8));
  }

  @Test
  @DisplayName("Should update auth_code_config partially - only provided fields")
  public void testUpdateAuthCodeConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_LENGTH, 8);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_LENGTH), equalTo(8));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_TTL), equalTo(300));
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
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
  }

  @Test
  @DisplayName("Should return error when ttl is less than 1 in update")
  public void testUpdateAuthCodeConfigTtlTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_TTL, 0);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TTL_MUST_BE_GREATER_THAN_0));
  }

  @Test
  @DisplayName("Should return error when length is less than 1 in update")
  public void testUpdateAuthCodeConfigLengthTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAuthCodeConfig(testTenantId, createAuthCodeConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_LENGTH, 0);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_LENGTH_MUST_BE_GREATER_THAN_0));
  }

  @Test
  @DisplayName("Should return 404 when auth_code_config not found for update")
  public void testUpdateAuthCodeConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_TTL, 600);

    Response response = updateAuthCodeConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_AUTH_CODE_CONFIG_NOT_FOUND));
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
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_AUTH_CODE_CONFIG_NOT_FOUND));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put(REQUEST_FIELD_ID, testTenantId);
    tenantBody.put(REQUEST_FIELD_NAME, testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createAuthCodeConfigBody() {
    Map<String, Object> authCodeConfigBody = new HashMap<>();
    authCodeConfigBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    authCodeConfigBody.put(REQUEST_FIELD_TTL, 300);
    authCodeConfigBody.put(REQUEST_FIELD_LENGTH, 6);
    return authCodeConfigBody;
  }
}
