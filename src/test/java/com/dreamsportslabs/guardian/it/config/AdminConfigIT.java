package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_PASSWORD_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_PASSWORD_CANNOT_EXCEED_50;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_USERNAME_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_USERNAME_CANNOT_EXCEED_50;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_NAME;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_PASSWORD;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_USERNAME;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createAdminConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteAdminConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getAdminConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateAdminConfig;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupChangelog;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.Setup;
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
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Setup.class)
public class AdminConfigIT {

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
  @DisplayName("Should create admin_config successfully")
  public void testCreateAdminConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createAdminConfig(testTenantId, createAdminConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_USERNAME), equalTo("admin"));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_PASSWORD), equalTo("password123"));

    JsonObject dbConfig = DbUtils.getAdminConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(dbConfig.getString(REQUEST_FIELD_USERNAME), equalTo("admin"));
    assertThat(dbConfig.getString(REQUEST_FIELD_PASSWORD), equalTo("password123"));
  }

  @Test
  @DisplayName("Should return error when username is blank")
  public void testCreateAdminConfigBlankUsername() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAdminConfigBody();
    requestBody.put(REQUEST_FIELD_USERNAME, "");

    Response response = createAdminConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_USERNAME_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when username exceeds 50 characters")
  public void testCreateAdminConfigUsernameTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longUsername = RandomStringUtils.randomAlphanumeric(51);
    Map<String, Object> requestBody = createAdminConfigBody();
    requestBody.put(REQUEST_FIELD_USERNAME, longUsername);

    Response response = createAdminConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_USERNAME_CANNOT_EXCEED_50));
  }

  @Test
  @DisplayName("Should return error when password is blank")
  public void testCreateAdminConfigBlankPassword() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAdminConfigBody();
    requestBody.put(REQUEST_FIELD_PASSWORD, "");

    Response response = createAdminConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PASSWORD_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when password exceeds 50 characters")
  public void testCreateAdminConfigPasswordTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longPassword = RandomStringUtils.randomAlphanumeric(51);
    Map<String, Object> requestBody = createAdminConfigBody();
    requestBody.put(REQUEST_FIELD_PASSWORD, longPassword);

    Response response = createAdminConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PASSWORD_CANNOT_EXCEED_50));
  }

  @Test
  @DisplayName("Should return error when admin_config already exists")
  public void testCreateAdminConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Response response = createAdminConfig(testTenantId, createAdminConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("admin_config_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("Admin config already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should get admin_config successfully")
  public void testGetAdminConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Response response = getAdminConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_USERNAME), equalTo("admin"));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_PASSWORD), equalTo("password123"));
  }

  @Test
  @DisplayName("Should return 404 when admin_config not found")
  public void testGetAdminConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getAdminConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("admin_config_not_found"));
  }

  @Test
  @DisplayName("Should update admin_config successfully with single field")
  public void testUpdateAdminConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_USERNAME, "newadmin");

    Response response = updateAdminConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("username"), equalTo("newadmin"));
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_PASSWORD), equalTo("password123"));

    JsonObject dbConfig = DbUtils.getAdminConfig(testTenantId);
    assertThat(dbConfig.getString("username"), equalTo("newadmin"));
  }

  @Test
  @DisplayName("Should update admin_config successfully with multiple fields")
  public void testUpdateAdminConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_USERNAME, "newadmin");
    updateBody.put(REQUEST_FIELD_PASSWORD, "newpassword123");

    Response response = updateAdminConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("username"), equalTo("newadmin"));
    assertThat(response.jsonPath().getString("password"), equalTo("newpassword123"));

    JsonObject dbConfig = DbUtils.getAdminConfig(testTenantId);
    assertThat(dbConfig.getString("username"), equalTo("newadmin"));
    assertThat(dbConfig.getString("password"), equalTo("newpassword123"));
  }

  @Test
  @DisplayName("Should update admin_config partially - only provided fields")
  public void testUpdateAdminConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_USERNAME, "newadmin");

    Response response = updateAdminConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("username"), equalTo("newadmin"));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_PASSWORD), equalTo("password123"));
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateAdminConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateAdminConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
  }

  @Test
  @DisplayName("Should return error when username is blank in update")
  public void testUpdateAdminConfigBlankUsername() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("username", "");

    Response response = updateAdminConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_USERNAME_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when username exceeds 50 characters in update")
  public void testUpdateAdminConfigUsernameTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("username", RandomStringUtils.randomAlphanumeric(51));

    Response response = updateAdminConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_USERNAME_CANNOT_EXCEED_50));
  }

  @Test
  @DisplayName("Should return error when password is blank in update")
  public void testUpdateAdminConfigBlankPassword() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("password", "");

    Response response = updateAdminConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PASSWORD_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when password exceeds 50 characters in update")
  public void testUpdateAdminConfigPasswordTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_PASSWORD, RandomStringUtils.randomAlphanumeric(51));

    Response response = updateAdminConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PASSWORD_CANNOT_EXCEED_50));
  }

  @Test
  @DisplayName("Should return 404 when admin_config not found for update")
  public void testUpdateAdminConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_USERNAME, "newadmin");

    Response response = updateAdminConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("admin_config_not_found"));
  }

  @Test
  @DisplayName("Should delete admin_config successfully")
  public void testDeleteAdminConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Response response = deleteAdminConfig(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getAdminConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 404 when admin_config not found for delete")
  public void testDeleteAdminConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteAdminConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("admin_config_not_found"));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put(REQUEST_FIELD_ID, testTenantId);
    tenantBody.put(REQUEST_FIELD_NAME, testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createAdminConfigBody() {
    Map<String, Object> adminConfigBody = new HashMap<>();
    adminConfigBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    adminConfigBody.put(REQUEST_FIELD_USERNAME, "admin");
    adminConfigBody.put(REQUEST_FIELD_PASSWORD, "password123");
    return adminConfigBody;
  }
}
