package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SECRET_KEY_CANNOT_EXCEED_16;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ALLOWED_SCOPES;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_IS_ENCRYPTED;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_NAME;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_SECRET_KEY;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
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

import com.dreamsportslabs.guardian.Setup;
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
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Setup.class)
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
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_ENCRYPTED), equalTo(true));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_SECRET_KEY), equalTo("secret123456"));
    assertThat(response.jsonPath().getList("allowed_scopes").size(), equalTo(2));

    JsonObject dbConfig = DbUtils.getGuestConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(dbConfig.getBoolean(REQUEST_FIELD_IS_ENCRYPTED), equalTo(true));
    assertThat(dbConfig.getString(REQUEST_FIELD_SECRET_KEY), equalTo("secret123456"));
  }

  @Test
  @DisplayName("Should create guest_config with default values when not provided")
  public void testCreateGuestConfigWithDefaults() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    List<String> allowedScopes = new ArrayList<>();
    allowedScopes.add("read");
    allowedScopes.add("write");
    requestBody.put(REQUEST_FIELD_ALLOWED_SCOPES, allowedScopes);

    Response response = createGuestConfig(testTenantId, requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_ENCRYPTED), equalTo(true));
    assertThat(response.jsonPath().getList("allowed_scopes").size(), equalTo(2));
  }

  @Test
  @DisplayName("Should create guest_config with null allowed_scopes (defaults to empty array)")
  public void testCreateGuestConfigWithNullAllowedScopes() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);

    Response response = createGuestConfig(testTenantId, requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getList("allowed_scopes").size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return error when secret_key exceeds 16 characters")
  public void testCreateGuestConfigSecretKeyTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createGuestConfigBody();
    requestBody.put(REQUEST_FIELD_SECRET_KEY, RandomStringUtils.randomAlphanumeric(17));

    Response response = createGuestConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_SECRET_KEY_CANNOT_EXCEED_16));
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
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_ENCRYPTED), equalTo(true));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_SECRET_KEY), equalTo("secret123456"));
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
    updateBody.put(REQUEST_FIELD_IS_ENCRYPTED, false);

    Response response = updateGuestConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_encrypted"), equalTo(false));
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_SECRET_KEY), equalTo("secret123456"));

    JsonObject dbConfig = DbUtils.getGuestConfig(testTenantId);
    assertThat(dbConfig.getBoolean(REQUEST_FIELD_IS_ENCRYPTED), equalTo(false));
  }

  @Test
  @DisplayName("Should update guest_config successfully with multiple fields")
  public void testUpdateGuestConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGuestConfig(testTenantId, createGuestConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_IS_ENCRYPTED, false);
    updateBody.put(REQUEST_FIELD_SECRET_KEY, "newsecret123");
    List<String> newScopes = new ArrayList<>();
    newScopes.add("read");
    newScopes.add("write");
    newScopes.add("admin");
    updateBody.put(REQUEST_FIELD_ALLOWED_SCOPES, newScopes);

    Response response = updateGuestConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_encrypted"), equalTo(false));
    assertThat(response.jsonPath().getString("secret_key"), equalTo("newsecret123"));
    assertThat(response.jsonPath().getList("allowed_scopes").size(), equalTo(3));

    JsonObject dbConfig = DbUtils.getGuestConfig(testTenantId);
    assertThat(dbConfig.getBoolean(REQUEST_FIELD_IS_ENCRYPTED), equalTo(false));
    assertThat(dbConfig.getString(REQUEST_FIELD_SECRET_KEY), equalTo("newsecret123"));
  }

  @Test
  @DisplayName("Should update guest_config partially - only provided fields")
  public void testUpdateGuestConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGuestConfig(testTenantId, createGuestConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_IS_ENCRYPTED, false);

    Response response = updateGuestConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_encrypted"), equalTo(false));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_SECRET_KEY), equalTo("secret123456"));
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
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
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
        equalTo(ERROR_MSG_SECRET_KEY_CANNOT_EXCEED_16));
  }

  @Test
  @DisplayName("Should return 404 when guest_config not found for update")
  public void testUpdateGuestConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_IS_ENCRYPTED, false);

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
    tenantBody.put(REQUEST_FIELD_ID, testTenantId);
    tenantBody.put(REQUEST_FIELD_NAME, testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createGuestConfigBody() {
    Map<String, Object> guestConfigBody = new HashMap<>();
    guestConfigBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    guestConfigBody.put(REQUEST_FIELD_IS_ENCRYPTED, true);
    guestConfigBody.put(REQUEST_FIELD_SECRET_KEY, "secret123456");
    List<String> allowedScopes = new ArrayList<>();
    allowedScopes.add("read");
    allowedScopes.add("write");
    guestConfigBody.put(REQUEST_FIELD_ALLOWED_SCOPES, allowedScopes);
    return guestConfigBody;
  }
}
