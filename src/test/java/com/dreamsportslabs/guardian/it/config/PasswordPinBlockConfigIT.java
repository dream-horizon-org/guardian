package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_PASSWORD_PIN_BLOCK_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_PASSWORD_PIN_BLOCK_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ATTEMPTS_ALLOWED;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ATTEMPTS_WINDOW_SECONDS;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_BLOCK_INTERVAL_SECONDS;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createPasswordPinBlockConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deletePasswordPinBlockConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getPasswordPinBlockConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updatePasswordPinBlockConfig;
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
public class PasswordPinBlockConfigIT {

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
  @DisplayName("Should create password_pin_block_config successfully")
  public void testCreatePasswordPinBlockConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createPasswordPinBlockConfig(testTenantId, createConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_ATTEMPTS_ALLOWED), equalTo(5));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_ATTEMPTS_WINDOW_SECONDS), equalTo(3600));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_BLOCK_INTERVAL_SECONDS), equalTo(86400));

    JsonObject dbConfig = DbUtils.getPasswordPinBlockConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_ATTEMPTS_ALLOWED), equalTo(5));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_ATTEMPTS_WINDOW_SECONDS), equalTo(3600));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_BLOCK_INTERVAL_SECONDS), equalTo(86400));
  }

  @Test
  @DisplayName("Should return error when creating duplicate password_pin_block_config")
  public void testCreatePasswordPinBlockConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createPasswordPinBlockConfig(testTenantId, createConfigBody()).then().statusCode(SC_CREATED);

    Response response = createPasswordPinBlockConfig(testTenantId, createConfigBody());

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_PASSWORD_PIN_BLOCK_CONFIG_ALREADY_EXISTS));
  }

  @Test
  @DisplayName("Should return error when attempts_allowed is less than 1")
  public void testCreatePasswordPinBlockConfigAttemptsAllowedTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createConfigBody();
    requestBody.put(REQUEST_FIELD_ATTEMPTS_ALLOWED, 0);

    Response response = createPasswordPinBlockConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
  }

  @Test
  @DisplayName("Should get password_pin_block_config successfully")
  public void testGetPasswordPinBlockConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createPasswordPinBlockConfig(testTenantId, createConfigBody()).then().statusCode(SC_CREATED);

    Response response = getPasswordPinBlockConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_ATTEMPTS_ALLOWED), equalTo(5));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_ATTEMPTS_WINDOW_SECONDS), equalTo(3600));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_BLOCK_INTERVAL_SECONDS), equalTo(86400));
  }

  @Test
  @DisplayName("Should return 404 when password_pin_block_config not found")
  public void testGetPasswordPinBlockConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getPasswordPinBlockConfig(testTenantId);

    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_PASSWORD_PIN_BLOCK_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should update password_pin_block_config single field")
  public void testUpdatePasswordPinBlockConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createPasswordPinBlockConfig(testTenantId, createConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_ATTEMPTS_ALLOWED, 10);

    Response response = updatePasswordPinBlockConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_ATTEMPTS_ALLOWED), equalTo(10));

    JsonObject dbConfig = DbUtils.getPasswordPinBlockConfig(testTenantId);
    assertThat(dbConfig.getInteger(REQUEST_FIELD_ATTEMPTS_ALLOWED), equalTo(10));
  }

  @Test
  @DisplayName("Should update password_pin_block_config multiple fields")
  public void testUpdatePasswordPinBlockConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createPasswordPinBlockConfig(testTenantId, createConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_ATTEMPTS_ALLOWED, 3);
    updateBody.put(REQUEST_FIELD_ATTEMPTS_WINDOW_SECONDS, 7200);
    updateBody.put(REQUEST_FIELD_BLOCK_INTERVAL_SECONDS, 43200);

    Response response = updatePasswordPinBlockConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_ATTEMPTS_ALLOWED), equalTo(3));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_ATTEMPTS_WINDOW_SECONDS), equalTo(7200));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_BLOCK_INTERVAL_SECONDS), equalTo(43200));

    JsonObject dbConfig = DbUtils.getPasswordPinBlockConfig(testTenantId);
    assertThat(dbConfig.getInteger(REQUEST_FIELD_ATTEMPTS_ALLOWED), equalTo(3));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_ATTEMPTS_WINDOW_SECONDS), equalTo(7200));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_BLOCK_INTERVAL_SECONDS), equalTo(43200));
  }

  @Test
  @DisplayName("Should return error when update has no fields")
  public void testUpdatePasswordPinBlockConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createPasswordPinBlockConfig(testTenantId, createConfigBody()).then().statusCode(SC_CREATED);

    Response response = updatePasswordPinBlockConfig(testTenantId, new HashMap<>());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_NO_FIELDS_TO_UPDATE));
  }

  @Test
  @DisplayName("Should return 404 when updating non-existent password_pin_block_config")
  public void testUpdatePasswordPinBlockConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_ATTEMPTS_ALLOWED, 10);

    Response response = updatePasswordPinBlockConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_PASSWORD_PIN_BLOCK_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should delete password_pin_block_config successfully")
  public void testDeletePasswordPinBlockConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createPasswordPinBlockConfig(testTenantId, createConfigBody()).then().statusCode(SC_CREATED);

    Response response = deletePasswordPinBlockConfig(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getPasswordPinBlockConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent password_pin_block_config")
  public void testDeletePasswordPinBlockConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deletePasswordPinBlockConfig(testTenantId);

    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo(ERROR_CODE_PASSWORD_PIN_BLOCK_CONFIG_NOT_FOUND));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> body = new HashMap<>();
    body.put("id", testTenantId);
    body.put("name", testTenantName);
    return body;
  }

  private Map<String, Object> createConfigBody() {
    Map<String, Object> body = new HashMap<>();
    body.put(REQUEST_FIELD_ATTEMPTS_ALLOWED, 5);
    body.put(REQUEST_FIELD_ATTEMPTS_WINDOW_SECONDS, 3600);
    body.put(REQUEST_FIELD_BLOCK_INTERVAL_SECONDS, 86400);
    return body;
  }
}
