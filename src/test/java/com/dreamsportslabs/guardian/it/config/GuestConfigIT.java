package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createGuestConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteGuestConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getGuestConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateGuestConfig;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GuestConfigIT {

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
  @DisplayName("Should create guest_config successfully")
  public void testCreateGuestConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createGuestConfig(testTenantId, createGuestConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean("is_encrypted"), equalTo(true));
    assertThat(response.jsonPath().getString("secret_key"), equalTo("secret123456"));
    assertThat(response.jsonPath().getList("allowed_scopes").size(), equalTo(2));

    JsonObject dbConfig = DbUtils.getGuestConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString("tenant_id"), equalTo(testTenantId));
    assertThat(dbConfig.getBoolean("is_encrypted"), equalTo(true));
    assertThat(dbConfig.getString("secret_key"), equalTo("secret123456"));
  }

  @Test
  @DisplayName("Should create guest_config with default values when not provided")
  public void testCreateGuestConfigWithDefaults() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tenant_id", testTenantId);
    List<String> allowedScopes = new ArrayList<>();
    allowedScopes.add("read");
    allowedScopes.add("write");
    requestBody.put("allowed_scopes", allowedScopes);

    Response response = createGuestConfig(testTenantId, requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getBoolean("is_encrypted"), equalTo(true));
    assertThat(response.jsonPath().getList("allowed_scopes").size(), equalTo(2));
  }

  @Test
  @DisplayName("Should create guest_config with null allowed_scopes (defaults to empty array)")
  public void testCreateGuestConfigWithNullAllowedScopes() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tenant_id", testTenantId);
    requestBody.put("allowed_scopes", null);

    Response response = createGuestConfig(testTenantId, requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getList("allowed_scopes").size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return error when tenant_id is blank")
  public void testCreateGuestConfigBlankTenantId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createGuestConfigBody();
    requestBody.put("tenant_id", "");

    Response response = createGuestConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("tenant_id cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when tenant_id exceeds 10 characters")
  public void testCreateGuestConfigTenantIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createGuestConfigBody();
    requestBody.put("tenant_id", RandomStringUtils.randomAlphanumeric(11));

    Response response = createGuestConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant_id cannot exceed 10 characters"));
  }

  @Test
  @DisplayName("Should return error when secret_key exceeds 16 characters")
  public void testCreateGuestConfigSecretKeyTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createGuestConfigBody();
    requestBody.put("secret_key", RandomStringUtils.randomAlphanumeric(17));

    Response response = createGuestConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("secret_key cannot exceed 16 characters"));
  }

  @Test
  @DisplayName("Should return error when tenant-id header doesn't match body tenant_id")
  public void testCreateGuestConfigHeaderMismatch() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createGuestConfigBody();
    String differentTenantId = "diff" + RandomStringUtils.randomAlphanumeric(6);
    requestBody.put("tenant_id", differentTenantId);

    Response response = createGuestConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant-id header must match tenant_id in request body"));
  }

  @Test
  @DisplayName("Should return error when guest_config already exists")
  public void testCreateGuestConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGuestConfig(testTenantId, createGuestConfigBody()).then().statusCode(SC_CREATED);

    Response response = createGuestConfig(testTenantId, createGuestConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("guest_config_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("Guest config already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should get guest_config successfully")
  public void testGetGuestConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGuestConfig(testTenantId, createGuestConfigBody()).then().statusCode(SC_CREATED);

    Response response = getGuestConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean("is_encrypted"), equalTo(true));
    assertThat(response.jsonPath().getString("secret_key"), equalTo("secret123456"));
    assertThat(response.jsonPath().getList("allowed_scopes").size(), equalTo(2));
  }

  @Test
  @DisplayName("Should return 404 when guest_config not found")
  public void testGetGuestConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getGuestConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("guest_config_not_found"));
  }

  @Test
  @DisplayName("Should update guest_config successfully with single field")
  public void testUpdateGuestConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGuestConfig(testTenantId, createGuestConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("is_encrypted", false);

    Response response = updateGuestConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_encrypted"), equalTo(false));
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("secret_key"), equalTo("secret123456"));

    JsonObject dbConfig = DbUtils.getGuestConfig(testTenantId);
    assertThat(dbConfig.getBoolean("is_encrypted"), equalTo(false));
  }

  @Test
  @DisplayName("Should update guest_config successfully with multiple fields")
  public void testUpdateGuestConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGuestConfig(testTenantId, createGuestConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("is_encrypted", false);
    updateBody.put("secret_key", "newsecret123");
    List<String> newScopes = new ArrayList<>();
    newScopes.add("read");
    newScopes.add("write");
    newScopes.add("admin");
    updateBody.put("allowed_scopes", newScopes);

    Response response = updateGuestConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_encrypted"), equalTo(false));
    assertThat(response.jsonPath().getString("secret_key"), equalTo("newsecret123"));
    assertThat(response.jsonPath().getList("allowed_scopes").size(), equalTo(3));

    JsonObject dbConfig = DbUtils.getGuestConfig(testTenantId);
    assertThat(dbConfig.getBoolean("is_encrypted"), equalTo(false));
    assertThat(dbConfig.getString("secret_key"), equalTo("newsecret123"));
  }

  @Test
  @DisplayName("Should update guest_config partially - only provided fields")
  public void testUpdateGuestConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGuestConfig(testTenantId, createGuestConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("is_encrypted", false);

    Response response = updateGuestConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_encrypted"), equalTo(false));
    assertThat(response.jsonPath().getString("secret_key"), equalTo("secret123456"));
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateGuestConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGuestConfig(testTenantId, createGuestConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateGuestConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("no_fields_to_update"));
  }

  @Test
  @DisplayName("Should return error when secret_key exceeds 16 characters in update")
  public void testUpdateGuestConfigSecretKeyTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGuestConfig(testTenantId, createGuestConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("secret_key", RandomStringUtils.randomAlphanumeric(17));

    Response response = updateGuestConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("secret_key cannot exceed 16 characters"));
  }

  @Test
  @DisplayName("Should return 404 when guest_config not found for update")
  public void testUpdateGuestConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("is_encrypted", false);

    Response response = updateGuestConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("guest_config_not_found"));
  }

  @Test
  @DisplayName("Should delete guest_config successfully")
  public void testDeleteGuestConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGuestConfig(testTenantId, createGuestConfigBody()).then().statusCode(SC_CREATED);

    Response response = deleteGuestConfig(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getGuestConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 404 when guest_config not found for delete")
  public void testDeleteGuestConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteGuestConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("guest_config_not_found"));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put("id", testTenantId);
    tenantBody.put("name", testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createGuestConfigBody() {
    Map<String, Object> guestConfigBody = new HashMap<>();
    guestConfigBody.put("tenant_id", testTenantId);
    guestConfigBody.put("is_encrypted", true);
    guestConfigBody.put("secret_key", "secret123456");
    List<String> allowedScopes = new ArrayList<>();
    allowedScopes.add("read");
    allowedScopes.add("write");
    guestConfigBody.put("allowed_scopes", allowedScopes);
    return guestConfigBody;
  }
}
