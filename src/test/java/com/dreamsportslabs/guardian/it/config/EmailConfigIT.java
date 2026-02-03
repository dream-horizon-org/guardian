package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_EMAIL_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_EMAIL_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_HOST_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_HOST_CANNOT_EXCEED_256;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_PORT_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_PORT_MUST_BE_LESS_THAN_OR_EQUAL_TO_65535;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SEND_EMAIL_PATH_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_SEND_EMAIL_PATH_CANNOT_EXCEED_256;
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
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_SEND_EMAIL_PATH;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TEMPLATE_NAME;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TEMPLATE_PARAMS;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createEmailConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteEmailConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getEmailConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateEmailConfig;
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
public class EmailConfigIT {

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
  @DisplayName("Should create email_config successfully")
  public void testCreateEmailConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createEmailConfig(testTenantId, createEmailConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(false));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_HOST), equalTo("smtp.example.com"));
    assertThat(response.jsonPath().getInt("port"), equalTo(587));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_SEND_EMAIL_PATH), equalTo("/send"));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_TEMPLATE_NAME), equalTo("welcome"));

    JsonObject dbConfig = DbUtils.getEmailConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString("tenant_id"), equalTo(testTenantId));
    assertThat(dbConfig.getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(false));
    assertThat(dbConfig.getString(REQUEST_FIELD_HOST), equalTo("smtp.example.com"));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_PORT), equalTo(587));
  }

  @Test
  @DisplayName("Should return error when host is blank")
  public void testCreateEmailConfigBlankHost() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put(REQUEST_FIELD_HOST, "");

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_HOST_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when host exceeds 256 characters")
  public void testCreateEmailConfigHostTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longHost = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put(REQUEST_FIELD_HOST, longHost);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_HOST_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when port is less than 1")
  public void testCreateEmailConfigPortTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put(REQUEST_FIELD_PORT, 0);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PORT_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1));
  }

  @Test
  @DisplayName("Should return error when port exceeds 65535")
  public void testCreateEmailConfigPortTooHigh() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put(REQUEST_FIELD_PORT, 65536);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PORT_MUST_BE_LESS_THAN_OR_EQUAL_TO_65535));
  }

  @Test
  @DisplayName("Should return error when send_email_path is blank")
  public void testCreateEmailConfigBlankSendEmailPath() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put(REQUEST_FIELD_SEND_EMAIL_PATH, "");

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_SEND_EMAIL_PATH_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when send_email_path exceeds 256 characters")
  public void testCreateEmailConfigSendEmailPathTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longPath = "/" + RandomStringUtils.randomAlphanumeric(256);
    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put(REQUEST_FIELD_SEND_EMAIL_PATH, longPath);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_SEND_EMAIL_PATH_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when template_name is blank")
  public void testCreateEmailConfigBlankTemplateName() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put(REQUEST_FIELD_TEMPLATE_NAME, "");

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TEMPLATE_NAME_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when template_name exceeds 256 characters")
  public void testCreateEmailConfigTemplateNameTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longTemplateName = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put(REQUEST_FIELD_TEMPLATE_NAME, longTemplateName);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TEMPLATE_NAME_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when template_params is null")
  public void testCreateEmailConfigNullTemplateParams() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put(REQUEST_FIELD_TEMPLATE_PARAMS, null);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("template_params cannot be null"));
  }

  @Test
  @DisplayName("Should return error when email_config already exists")
  public void testCreateEmailConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Response response = createEmailConfig(testTenantId, createEmailConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_EMAIL_CONFIG_ALREADY_EXISTS));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("Email config already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should get email_config successfully")
  public void testGetEmailConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Response response = getEmailConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(false));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_HOST), equalTo("smtp.example.com"));
    assertThat(response.jsonPath().getInt("port"), equalTo(587));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_SEND_EMAIL_PATH), equalTo("/send"));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_TEMPLATE_NAME), equalTo("welcome"));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for get")
  public void testGetEmailConfigMissingHeader() {
    Response response = given().get("/v1/admin/config/email");
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 404 when email_config not found")
  public void testGetEmailConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getEmailConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_EMAIL_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should update email_config successfully with single field")
  public void testUpdateEmailConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_HOST, "updated-smtp.example.com");

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("host"), equalTo("updated-smtp.example.com"));
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getEmailConfig(testTenantId);
    assertThat(dbConfig.getString("host"), equalTo("updated-smtp.example.com"));
  }

  @Test
  @DisplayName("Should update email_config successfully with multiple fields")
  public void testUpdateEmailConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_IS_SSL_ENABLED, true);
    updateBody.put(REQUEST_FIELD_HOST, "secure-smtp.example.com");
    updateBody.put(REQUEST_FIELD_PORT, 465);
    updateBody.put(REQUEST_FIELD_SEND_EMAIL_PATH, "/api/send");
    updateBody.put(REQUEST_FIELD_TEMPLATE_NAME, "updated-welcome");

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_ssl_enabled"), equalTo(true));
    assertThat(response.jsonPath().getString("host"), equalTo("secure-smtp.example.com"));
    assertThat(response.jsonPath().getInt("port"), equalTo(465));
    assertThat(response.jsonPath().getString("send_email_path"), equalTo("/api/send"));
    assertThat(response.jsonPath().getString("template_name"), equalTo("updated-welcome"));

    JsonObject dbConfig = DbUtils.getEmailConfig(testTenantId);
    assertThat(dbConfig.getBoolean("is_ssl_enabled"), equalTo(true));
    assertThat(dbConfig.getString("host"), equalTo("secure-smtp.example.com"));
    assertThat(dbConfig.getInteger("port"), equalTo(465));
    assertThat(dbConfig.getString(REQUEST_FIELD_SEND_EMAIL_PATH), equalTo("/api/send"));
    assertThat(dbConfig.getString(REQUEST_FIELD_TEMPLATE_NAME), equalTo("updated-welcome"));
  }

  @Test
  @DisplayName("Should update email_config partially - only provided fields")
  public void testUpdateEmailConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_PORT, 2525);

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("port"), equalTo(2525));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_HOST), equalTo("smtp.example.com"));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(false));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for update")
  public void testUpdateEmailConfigMissingHeader() {
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_HOST, "updated-smtp.example.com");

    Response response =
        given()
            .header("Content-Type", "application/json")
            .body(updateBody)
            .patch("/v1/admin/config/email");

    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateEmailConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateEmailConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
  }

  @Test
  @DisplayName("Should return error when host is blank in update")
  public void testUpdateEmailConfigBlankHost() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("host", "");

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_HOST_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when host exceeds 256 characters in update")
  public void testUpdateEmailConfigHostTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    String longHost = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("host", longHost);

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_HOST_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when port is less than 1 in update")
  public void testUpdateEmailConfigPortTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("port", 0);

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PORT_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1));
  }

  @Test
  @DisplayName("Should return error when port exceeds 65535 in update")
  public void testUpdateEmailConfigPortTooHigh() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("port", 65536);

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PORT_MUST_BE_LESS_THAN_OR_EQUAL_TO_65535));
  }

  @Test
  @DisplayName("Should return error when send_email_path is blank in update")
  public void testUpdateEmailConfigBlankSendEmailPath() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("send_email_path", "");

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_SEND_EMAIL_PATH_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when send_email_path exceeds 256 characters in update")
  public void testUpdateEmailConfigSendEmailPathTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    String longPath = "/" + RandomStringUtils.randomAlphanumeric(256);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("send_email_path", longPath);

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_SEND_EMAIL_PATH_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when template_name is blank in update")
  public void testUpdateEmailConfigBlankTemplateName() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("template_name", "");

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TEMPLATE_NAME_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when template_name exceeds 256 characters in update")
  public void testUpdateEmailConfigTemplateNameTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    String longTemplateName = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("template_name", longTemplateName);

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TEMPLATE_NAME_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should update template_params successfully")
  public void testUpdateEmailConfigTemplateParams() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, String> newTemplateParams = new HashMap<>();
    newTemplateParams.put("key1", "value1");
    newTemplateParams.put("key2", "value2");

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_TEMPLATE_PARAMS, newTemplateParams);

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    Map<String, String> templateParams = response.jsonPath().getMap(REQUEST_FIELD_TEMPLATE_PARAMS);
    assertThat(templateParams.get("key1"), equalTo("value1"));
    assertThat(templateParams.get("key2"), equalTo("value2"));
  }

  @Test
  @DisplayName("Should return 404 when email_config not found for update")
  public void testUpdateEmailConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_HOST, "updated-smtp.example.com");

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_EMAIL_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should delete email_config successfully")
  public void testDeleteEmailConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Response response = deleteEmailConfig(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getEmailConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for delete")
  public void testDeleteEmailConfigMissingHeader() {
    Response response = given().delete("/v1/admin/config/email");
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 404 when email_config not found for delete")
  public void testDeleteEmailConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteEmailConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_EMAIL_CONFIG_NOT_FOUND));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put(REQUEST_FIELD_ID, testTenantId);
    tenantBody.put(REQUEST_FIELD_NAME, testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createEmailConfigBody() {
    Map<String, Object> emailConfigBody = new HashMap<>();
    emailConfigBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    emailConfigBody.put(REQUEST_FIELD_IS_SSL_ENABLED, false);
    emailConfigBody.put(REQUEST_FIELD_HOST, "smtp.example.com");
    emailConfigBody.put(REQUEST_FIELD_PORT, 587);
    emailConfigBody.put(REQUEST_FIELD_SEND_EMAIL_PATH, "/send");
    emailConfigBody.put(REQUEST_FIELD_TEMPLATE_NAME, "welcome");
    Map<String, String> templateParams = new HashMap<>();
    templateParams.put("name", "John");
    templateParams.put("email", "john@example.com");
    emailConfigBody.put(REQUEST_FIELD_TEMPLATE_PARAMS, templateParams);
    return emailConfigBody;
  }
}
