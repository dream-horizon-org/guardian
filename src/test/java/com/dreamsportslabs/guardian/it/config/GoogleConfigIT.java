package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_GOOGLE_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_GOOGLE_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_CLIENT_ID_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_CLIENT_ID_CANNOT_EXCEED_256;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_CLIENT_SECRET_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_CLIENT_SECRET_CANNOT_EXCEED_256;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_CLIENT_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_CLIENT_SECRET;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_NAME;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
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
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_CLIENT_ID),
        equalTo("123456789.apps.googleusercontent.com"));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_CLIENT_SECRET), equalTo("GOCSPX-secret123"));

    JsonObject dbConfig = DbUtils.getGoogleConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(
        dbConfig.getString(REQUEST_FIELD_CLIENT_ID),
        equalTo("123456789.apps.googleusercontent.com"));
    assertThat(dbConfig.getString(REQUEST_FIELD_CLIENT_SECRET), equalTo("GOCSPX-secret123"));
  }

  @Test
  @DisplayName("Should return error when client_id is blank")
  public void testCreateGoogleConfigBlankClientId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createGoogleConfigBody();
    requestBody.put(REQUEST_FIELD_CLIENT_ID, "");

    Response response = createGoogleConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_ID_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when client_id exceeds 256 characters")
  public void testCreateGoogleConfigClientIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longClientId = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createGoogleConfigBody();
    requestBody.put(REQUEST_FIELD_CLIENT_ID, longClientId);

    Response response = createGoogleConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_ID_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when client_secret is blank")
  public void testCreateGoogleConfigBlankClientSecret() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createGoogleConfigBody();
    requestBody.put(REQUEST_FIELD_CLIENT_SECRET, "");

    Response response = createGoogleConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_SECRET_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when client_secret exceeds 256 characters")
  public void testCreateGoogleConfigClientSecretTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longClientSecret = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createGoogleConfigBody();
    requestBody.put(REQUEST_FIELD_CLIENT_SECRET, longClientSecret);

    Response response = createGoogleConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_SECRET_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when google_config already exists")
  public void testCreateGoogleConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Response response = createGoogleConfig(testTenantId, createGoogleConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_GOOGLE_CONFIG_ALREADY_EXISTS));
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
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_CLIENT_ID),
        equalTo("123456789.apps.googleusercontent.com"));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_CLIENT_SECRET), equalTo("GOCSPX-secret123"));
  }

  @Test
  @DisplayName("Should return 404 when google_config not found")
  public void testGetGoogleConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getGoogleConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_GOOGLE_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should update google_config successfully with single field")
  public void testUpdateGoogleConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_CLIENT_ID, "987654321.apps.googleusercontent.com");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_CLIENT_ID),
        equalTo("987654321.apps.googleusercontent.com"));
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getGoogleConfig(testTenantId);
    assertThat(
        dbConfig.getString(REQUEST_FIELD_CLIENT_ID),
        equalTo("987654321.apps.googleusercontent.com"));
  }

  @Test
  @DisplayName("Should update google_config successfully with multiple fields")
  public void testUpdateGoogleConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_CLIENT_ID, "987654321.apps.googleusercontent.com");
    updateBody.put(REQUEST_FIELD_CLIENT_SECRET, "GOCSPX-newsecret456");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_CLIENT_ID),
        equalTo("987654321.apps.googleusercontent.com"));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_CLIENT_SECRET), equalTo("GOCSPX-newsecret456"));

    JsonObject dbConfig = DbUtils.getGoogleConfig(testTenantId);
    assertThat(
        dbConfig.getString(REQUEST_FIELD_CLIENT_ID),
        equalTo("987654321.apps.googleusercontent.com"));
    assertThat(dbConfig.getString(REQUEST_FIELD_CLIENT_SECRET), equalTo("GOCSPX-newsecret456"));
  }

  @Test
  @DisplayName("Should update google_config partially - only provided fields")
  public void testUpdateGoogleConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_CLIENT_SECRET, "GOCSPX-updatedsecret");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_CLIENT_SECRET),
        equalTo("GOCSPX-updatedsecret"));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_CLIENT_ID),
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
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
  }

  @Test
  @DisplayName("Should return error when client_id is blank in update")
  public void testUpdateGoogleConfigBlankClientId() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_CLIENT_ID, "");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_ID_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when client_id exceeds 256 characters in update")
  public void testUpdateGoogleConfigClientIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    String longClientId = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_CLIENT_ID, longClientId);

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_ID_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when client_secret is blank in update")
  public void testUpdateGoogleConfigBlankClientSecret() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_CLIENT_SECRET, "");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_SECRET_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when client_secret exceeds 256 characters in update")
  public void testUpdateGoogleConfigClientSecretTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createGoogleConfig(testTenantId, createGoogleConfigBody()).then().statusCode(SC_CREATED);

    String longClientSecret = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_CLIENT_SECRET, longClientSecret);

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_SECRET_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return 404 when google_config not found for update")
  public void testUpdateGoogleConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_CLIENT_ID, "987654321.apps.googleusercontent.com");

    Response response = updateGoogleConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_GOOGLE_CONFIG_NOT_FOUND));
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
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_GOOGLE_CONFIG_NOT_FOUND));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put(REQUEST_FIELD_ID, testTenantId);
    tenantBody.put(REQUEST_FIELD_NAME, testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createGoogleConfigBody() {
    Map<String, Object> googleConfigBody = new HashMap<>();
    googleConfigBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    googleConfigBody.put(REQUEST_FIELD_CLIENT_ID, "123456789.apps.googleusercontent.com");
    googleConfigBody.put(REQUEST_FIELD_CLIENT_SECRET, "GOCSPX-secret123");
    return googleConfigBody;
  }
}
