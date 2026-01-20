package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_SMS_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_SMS_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_HOST_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_HOST_CANNOT_EXCEED_256;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_PORT_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_PORT_MUST_BE_LESS_THAN_OR_EQUAL_TO_65535;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SEND_SMS_PATH_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SEND_SMS_PATH_CANNOT_EXCEED_256;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_TEMPLATE_NAME_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_TEMPLATE_NAME_CANNOT_EXCEED_256;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_HOST;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_NAME;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_PORT;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_SEND_SMS_PATH;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TEMPLATE_NAME;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TEMPLATE_PARAMS;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createSmsConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteSmsConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getSmsConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateSmsConfig;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupChangelog;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
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
public class SmsConfigIT {

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
  @DisplayName("Should create sms_config successfully")
  public void testCreateSmsConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createSmsConfig(testTenantId, createSmsConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(false));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_HOST), equalTo("sms.example.com"));
    assertThat(response.jsonPath().getInt("port"), equalTo(443));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_SEND_SMS_PATH), equalTo("/send"));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_TEMPLATE_NAME), equalTo("welcome"));

    JsonObject dbConfig = DbUtils.getSmsConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(dbConfig.getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(false));
    assertThat(dbConfig.getString(REQUEST_FIELD_HOST), equalTo("sms.example.com"));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_PORT), equalTo(443));
  }

  @Test
  @DisplayName("Should return error when host is blank")
  public void testCreateSmsConfigBlankHost() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createSmsConfigBody();
    requestBody.put(REQUEST_FIELD_HOST, "");

    Response response = createSmsConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_HOST_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when host exceeds 256 characters")
  public void testCreateSmsConfigHostTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longHost = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createSmsConfigBody();
    requestBody.put(REQUEST_FIELD_HOST, longHost);

    Response response = createSmsConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_HOST_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when port is less than 1")
  public void testCreateSmsConfigPortTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createSmsConfigBody();
    requestBody.put(REQUEST_FIELD_PORT, 0);

    Response response = createSmsConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PORT_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1));
  }

  @Test
  @DisplayName("Should return error when port exceeds 65535")
  public void testCreateSmsConfigPortTooHigh() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createSmsConfigBody();
    requestBody.put(REQUEST_FIELD_PORT, 65536);

    Response response = createSmsConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PORT_MUST_BE_LESS_THAN_OR_EQUAL_TO_65535));
  }

  @Test
  @DisplayName("Should return error when send_sms_path is blank")
  public void testCreateSmsConfigBlankSendSmsPath() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createSmsConfigBody();
    requestBody.put(REQUEST_FIELD_SEND_SMS_PATH, "");

    Response response = createSmsConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_SEND_SMS_PATH_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when send_sms_path exceeds 256 characters")
  public void testCreateSmsConfigSendSmsPathTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longPath = "/" + RandomStringUtils.randomAlphanumeric(256);
    Map<String, Object> requestBody = createSmsConfigBody();
    requestBody.put(REQUEST_FIELD_SEND_SMS_PATH, longPath);

    Response response = createSmsConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_SEND_SMS_PATH_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when template_name is blank")
  public void testCreateSmsConfigBlankTemplateName() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createSmsConfigBody();
    requestBody.put(REQUEST_FIELD_TEMPLATE_NAME, "");

    Response response = createSmsConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TEMPLATE_NAME_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when template_name exceeds 256 characters")
  public void testCreateSmsConfigTemplateNameTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longTemplateName = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createSmsConfigBody();
    requestBody.put(REQUEST_FIELD_TEMPLATE_NAME, longTemplateName);

    Response response = createSmsConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TEMPLATE_NAME_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when template_params is null")
  public void testCreateSmsConfigNullTemplateParams() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createSmsConfigBody();
    requestBody.put(REQUEST_FIELD_TEMPLATE_PARAMS, null);

    Response response = createSmsConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("template_params cannot be null"));
  }

  @Test
  @DisplayName("Should return error when sms_config already exists")
  public void testCreateSmsConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Response response = createSmsConfig(testTenantId, createSmsConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_SMS_CONFIG_ALREADY_EXISTS));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("SMS config already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should get sms_config successfully")
  public void testGetSmsConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Response response = getSmsConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(false));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_HOST), equalTo("sms.example.com"));
    assertThat(response.jsonPath().getInt("port"), equalTo(443));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_SEND_SMS_PATH), equalTo("/send"));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_TEMPLATE_NAME), equalTo("welcome"));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for get")
  public void testGetSmsConfigMissingHeader() {
    Response response = given().get("/v1/admin/config/sms");
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 404 when sms_config not found")
  public void testGetSmsConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getSmsConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_SMS_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should update sms_config successfully with single field")
  public void testUpdateSmsConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_HOST, "updated-sms.example.com");

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("host"), equalTo("updated-sms.example.com"));
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getSmsConfig(testTenantId);
    assertThat(dbConfig.getString(REQUEST_FIELD_HOST), equalTo("updated-sms.example.com"));
  }

  @Test
  @DisplayName("Should update sms_config successfully with multiple fields")
  public void testUpdateSmsConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_IS_SSL_ENABLED, true);
    updateBody.put(REQUEST_FIELD_HOST, "secure-sms.example.com");
    updateBody.put(REQUEST_FIELD_PORT, 465);
    updateBody.put(REQUEST_FIELD_SEND_SMS_PATH, "/api/send");
    updateBody.put(REQUEST_FIELD_TEMPLATE_NAME, "updated-welcome");

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(true));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_HOST), equalTo("secure-sms.example.com"));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_PORT), equalTo(465));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_SEND_SMS_PATH), equalTo("/api/send"));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_TEMPLATE_NAME), equalTo("updated-welcome"));

    JsonObject dbConfig = DbUtils.getSmsConfig(testTenantId);
    assertThat(dbConfig.getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(true));
    assertThat(dbConfig.getString(REQUEST_FIELD_HOST), equalTo("secure-sms.example.com"));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_PORT), equalTo(465));
    assertThat(dbConfig.getString(REQUEST_FIELD_SEND_SMS_PATH), equalTo("/api/send"));
    assertThat(dbConfig.getString(REQUEST_FIELD_TEMPLATE_NAME), equalTo("updated-welcome"));
  }

  @Test
  @DisplayName("Should update sms_config partially - only provided fields")
  public void testUpdateSmsConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_PORT, 2525);

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("port"), equalTo(2525));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_HOST), equalTo("sms.example.com"));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(false));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for update")
  public void testUpdateSmsConfigMissingHeader() {
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_HOST, "updated-sms.example.com");

    Response response =
        given()
            .header("Content-Type", "application/json")
            .body(updateBody)
            .patch("/v1/admin/config/sms");

    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateSmsConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateSmsConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
  }

  @Test
  @DisplayName("Should return error when host is blank in update")
  public void testUpdateSmsConfigBlankHost() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("host", "");

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_HOST_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when host exceeds 256 characters in update")
  public void testUpdateSmsConfigHostTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    String longHost = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("host", longHost);

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_HOST_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when port is less than 1 in update")
  public void testUpdateSmsConfigPortTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("port", 0);

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PORT_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1));
  }

  @Test
  @DisplayName("Should return error when port exceeds 65535 in update")
  public void testUpdateSmsConfigPortTooHigh() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("port", 65536);

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PORT_MUST_BE_LESS_THAN_OR_EQUAL_TO_65535));
  }

  @Test
  @DisplayName("Should return error when send_sms_path is blank in update")
  public void testUpdateSmsConfigBlankSendSmsPath() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("send_sms_path", "");

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_SEND_SMS_PATH_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when send_sms_path exceeds 256 characters in update")
  public void testUpdateSmsConfigSendSmsPathTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    String longPath = "/" + RandomStringUtils.randomAlphanumeric(256);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("send_sms_path", longPath);

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_SEND_SMS_PATH_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when template_name is blank in update")
  public void testUpdateSmsConfigBlankTemplateName() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("template_name", "");

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TEMPLATE_NAME_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when template_name exceeds 256 characters in update")
  public void testUpdateSmsConfigTemplateNameTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    String longTemplateName = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("template_name", longTemplateName);

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TEMPLATE_NAME_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should update template_params successfully")
  public void testUpdateSmsConfigTemplateParams() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Map<String, String> newTemplateParams = new HashMap<>();
    newTemplateParams.put("key1", "value1");
    newTemplateParams.put("key2", "value2");

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_TEMPLATE_PARAMS, newTemplateParams);

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    Map<String, String> templateParams = response.jsonPath().getMap(REQUEST_FIELD_TEMPLATE_PARAMS);
    assertThat(templateParams.get("key1"), equalTo("value1"));
    assertThat(templateParams.get("key2"), equalTo("value2"));
  }

  @Test
  @DisplayName("Should return 404 when sms_config not found for update")
  public void testUpdateSmsConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_HOST, "updated-sms.example.com");

    Response response = updateSmsConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_SMS_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should delete sms_config successfully")
  public void testDeleteSmsConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createSmsConfig(testTenantId, createSmsConfigBody()).then().statusCode(SC_CREATED);

    Response response = deleteSmsConfig(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getSmsConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for delete")
  public void testDeleteSmsConfigMissingHeader() {
    Response response = given().delete("/v1/admin/config/sms");
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 404 when sms_config not found for delete")
  public void testDeleteSmsConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteSmsConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_SMS_CONFIG_NOT_FOUND));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put(REQUEST_FIELD_ID, testTenantId);
    tenantBody.put(REQUEST_FIELD_NAME, testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createSmsConfigBody() {
    Map<String, Object> smsConfigBody = new HashMap<>();
    smsConfigBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    smsConfigBody.put(REQUEST_FIELD_IS_SSL_ENABLED, false);
    smsConfigBody.put(REQUEST_FIELD_HOST, "sms.example.com");
    smsConfigBody.put(REQUEST_FIELD_PORT, 443);
    smsConfigBody.put(REQUEST_FIELD_SEND_SMS_PATH, "/send");
    smsConfigBody.put(REQUEST_FIELD_TEMPLATE_NAME, "welcome");
    Map<String, String> templateParams = new HashMap<>();
    templateParams.put("name", "John");
    templateParams.put("phone", "+1234567890");
    smsConfigBody.put(REQUEST_FIELD_TEMPLATE_PARAMS, templateParams);
    return smsConfigBody;
  }
}
