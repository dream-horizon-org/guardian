package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createGoogleConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteGoogleConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getGoogleConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateGoogleConfig;
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

public class GoogleConfigIT {

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
  @DisplayName("Should create google_config successfully")
  public void testCreateGoogleConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createGoogleConfig(testTenantId, createGoogleConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(
        response.jsonPath().getString("client_id"),
        equalTo("123456789.apps.googleusercontent.com"));
    assertThat(response.jsonPath().getString("client_secret"), equalTo("GOCSPX-secret123"));

    JsonObject dbConfig = DbUtils.getGoogleConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString("tenant_id"), equalTo(testTenantId));
    assertThat(dbConfig.getString("client_id"), equalTo("123456789.apps.googleusercontent.com"));
    assertThat(dbConfig.getString("client_secret"), equalTo("GOCSPX-secret123"));
  }

  @Test
  @DisplayName("Should return error when client_id is blank")
  public void testCreateGoogleConfigBlankClientId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createGoogleConfigBody();
    requestBody.put("client_id", "");

    Response response = createGoogleConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("client_id cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when client_id exceeds 256 characters")
  public void testCreateGoogleConfigClientIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longClientId = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createGoogleConfigBody();
    requestBody.put("client_id", longClientId);

    Response response = createGoogleConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("client_id cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when client_secret is blank")
  public void testCreateGoogleConfigBlankClientSecret() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createGoogleConfigBody();
    requestBody.put("client_secret", "");

    Response response = createGoogleConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("client_secret cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when client_secret exceeds 256 characters")
  public void testCreateGoogleConfigClientSecretTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longClientSecret = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createGoogleConfigBody();
    requestBody.put("client_secret", longClientSecret);

    Response response = createGoogleConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("client_secret cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when tenant-id header doesn't match body tenant_id")
  public void testCreateGoogleConfigHeaderMismatch() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createGoogleConfigBody();
    String differentTenantId = "diff" + RandomStringUtils.randomAlphanumeric(6);
    requestBody.put("tenant_id", differentTenantId);

    Response response = createGoogleConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant-id header must match tenant_id in request body"));
  }

  @Test
  @DisplayName("Should return error when google_config already exists")
  public void testCreateGoogleConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Response response = createGoogleConfig(testTenantId, createGoogleConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("google_config_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("Google config already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should get google_config successfully")
  public void testGetGoogleConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Response response = getGoogleConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(
        response.jsonPath().getString("client_id"),
        equalTo("123456789.apps.googleusercontent.com"));
    assertThat(response.jsonPath().getString("client_secret"), equalTo("GOCSPX-secret123"));
  }

  @Test
  @DisplayName("Should return 404 when google_config not found")
  public void testGetGoogleConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getGoogleConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("google_config_not_found"));
  }

  @Test
  @DisplayName("Should update google_config successfully with single field")
  public void testUpdateGoogleConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("client_id", "987654321.apps.googleusercontent.com");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(
        response.jsonPath().getString("client_id"),
        equalTo("987654321.apps.googleusercontent.com"));
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getGoogleConfig(testTenantId);
    assertThat(dbConfig.getString("client_id"), equalTo("987654321.apps.googleusercontent.com"));
  }

  @Test
  @DisplayName("Should update google_config successfully with multiple fields")
  public void testUpdateGoogleConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("client_id", "987654321.apps.googleusercontent.com");
    updateBody.put("client_secret", "GOCSPX-newsecret456");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(
        response.jsonPath().getString("client_id"),
        equalTo("987654321.apps.googleusercontent.com"));
    assertThat(response.jsonPath().getString("client_secret"), equalTo("GOCSPX-newsecret456"));

    JsonObject dbConfig = DbUtils.getGoogleConfig(testTenantId);
    assertThat(dbConfig.getString("client_id"), equalTo("987654321.apps.googleusercontent.com"));
    assertThat(dbConfig.getString("client_secret"), equalTo("GOCSPX-newsecret456"));
  }

  @Test
  @DisplayName("Should update google_config partially - only provided fields")
  public void testUpdateGoogleConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("client_secret", "GOCSPX-updatedsecret");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("client_secret"), equalTo("GOCSPX-updatedsecret"));
    assertThat(
        response.jsonPath().getString("client_id"),
        equalTo("123456789.apps.googleusercontent.com"));
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateGoogleConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("no_fields_to_update"));
  }

  @Test
  @DisplayName("Should return error when client_id is blank in update")
  public void testUpdateGoogleConfigBlankClientId() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("client_id", "");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("client_id cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when client_id exceeds 256 characters in update")
  public void testUpdateGoogleConfigClientIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    String longClientId = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("client_id", longClientId);

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("client_id cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when client_secret is blank in update")
  public void testUpdateGoogleConfigBlankClientSecret() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("client_secret", "");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("client_secret cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when client_secret exceeds 256 characters in update")
  public void testUpdateGoogleConfigClientSecretTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    String longClientSecret = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("client_secret", longClientSecret);

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("client_secret cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return 404 when google_config not found for update")
  public void testUpdateGoogleConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("client_id", "987654321.apps.googleusercontent.com");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("google_config_not_found"));
  }

  @Test
  @DisplayName("Should delete google_config successfully")
  public void testDeleteGoogleConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Response response = deleteGoogleConfig(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getGoogleConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 404 when google_config not found for delete")
  public void testDeleteGoogleConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteGoogleConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("google_config_not_found"));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put("id", testTenantId);
    tenantBody.put("name", testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createGoogleConfigBody() {
    Map<String, Object> googleConfigBody = new HashMap<>();
    googleConfigBody.put("tenant_id", testTenantId);
    googleConfigBody.put("client_id", "123456789.apps.googleusercontent.com");
    googleConfigBody.put("client_secret", "GOCSPX-secret123");
    return googleConfigBody;
  }
}
