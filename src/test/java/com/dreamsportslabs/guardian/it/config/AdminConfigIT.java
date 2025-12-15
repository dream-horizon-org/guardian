package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
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
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("username"), equalTo("admin"));
    assertThat(response.jsonPath().getString("password"), equalTo("password123"));

    JsonObject dbConfig = DbUtils.getAdminConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString("tenant_id"), equalTo(testTenantId));
    assertThat(dbConfig.getString("username"), equalTo("admin"));
    assertThat(dbConfig.getString("password"), equalTo("password123"));
  }

  @Test
  @DisplayName("Should return error when tenant_id is blank")
  public void testCreateAdminConfigBlankTenantId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAdminConfigBody();
    requestBody.put("tenant_id", "");

    Response response = createAdminConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("tenant_id cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when tenant_id exceeds 10 characters")
  public void testCreateAdminConfigTenantIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAdminConfigBody();
    requestBody.put("tenant_id", RandomStringUtils.randomAlphanumeric(11));

    Response response = createAdminConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant_id cannot exceed 10 characters"));
  }

  @Test
  @DisplayName("Should return error when username is blank")
  public void testCreateAdminConfigBlankUsername() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAdminConfigBody();
    requestBody.put("username", "");

    Response response = createAdminConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("username cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when username exceeds 50 characters")
  public void testCreateAdminConfigUsernameTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longUsername = RandomStringUtils.randomAlphanumeric(51);
    Map<String, Object> requestBody = createAdminConfigBody();
    requestBody.put("username", longUsername);

    Response response = createAdminConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("username cannot exceed 50 characters"));
  }

  @Test
  @DisplayName("Should return error when password is blank")
  public void testCreateAdminConfigBlankPassword() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAdminConfigBody();
    requestBody.put("password", "");

    Response response = createAdminConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("password cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when password exceeds 50 characters")
  public void testCreateAdminConfigPasswordTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longPassword = RandomStringUtils.randomAlphanumeric(51);
    Map<String, Object> requestBody = createAdminConfigBody();
    requestBody.put("password", longPassword);

    Response response = createAdminConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("password cannot exceed 50 characters"));
  }

  @Test
  @DisplayName("Should return error when tenant-id header doesn't match body tenant_id")
  public void testCreateAdminConfigHeaderMismatch() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createAdminConfigBody();
    String differentTenantId = "diff" + RandomStringUtils.randomAlphanumeric(6);
    requestBody.put("tenant_id", differentTenantId);

    Response response = createAdminConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant-id header must match tenant_id in request body"));
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
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("username"), equalTo("admin"));
    assertThat(response.jsonPath().getString("password"), equalTo("password123"));
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
    updateBody.put("username", "newadmin");

    Response response = updateAdminConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("username"), equalTo("newadmin"));
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("password"), equalTo("password123"));

    JsonObject dbConfig = DbUtils.getAdminConfig(testTenantId);
    assertThat(dbConfig.getString("username"), equalTo("newadmin"));
  }

  @Test
  @DisplayName("Should update admin_config successfully with multiple fields")
  public void testUpdateAdminConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("username", "newadmin");
    updateBody.put("password", "newpassword123");

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
    updateBody.put("username", "newadmin");

    Response response = updateAdminConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("username"), equalTo("newadmin"));
    assertThat(response.jsonPath().getString("password"), equalTo("password123"));
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
        .body(CODE, equalTo("no_fields_to_update"));
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
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("username cannot be blank"));
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
        equalTo("username cannot exceed 50 characters"));
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
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("password cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when password exceeds 50 characters in update")
  public void testUpdateAdminConfigPasswordTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createAdminConfig(testTenantId, createAdminConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("password", RandomStringUtils.randomAlphanumeric(51));

    Response response = updateAdminConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("password cannot exceed 50 characters"));
  }

  @Test
  @DisplayName("Should return 404 when admin_config not found for update")
  public void testUpdateAdminConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("username", "newadmin");

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
    tenantBody.put("id", testTenantId);
    tenantBody.put("name", testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createAdminConfigBody() {
    Map<String, Object> adminConfigBody = new HashMap<>();
    adminConfigBody.put("tenant_id", testTenantId);
    adminConfigBody.put("username", "admin");
    adminConfigBody.put("password", "password123");
    return adminConfigBody;
  }
}
