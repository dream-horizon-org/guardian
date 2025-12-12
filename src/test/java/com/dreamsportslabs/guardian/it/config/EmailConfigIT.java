package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
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
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean("is_ssl_enabled"), equalTo(false));
    assertThat(response.jsonPath().getString("host"), equalTo("smtp.example.com"));
    assertThat(response.jsonPath().getInt("port"), equalTo(587));
    assertThat(response.jsonPath().getString("send_email_path"), equalTo("/send"));
    assertThat(response.jsonPath().getString("template_name"), equalTo("welcome"));

    JsonObject dbConfig = DbUtils.getEmailConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString("tenant_id"), equalTo(testTenantId));
    assertThat(dbConfig.getBoolean("is_ssl_enabled"), equalTo(false));
    assertThat(dbConfig.getString("host"), equalTo("smtp.example.com"));
    assertThat(dbConfig.getInteger("port"), equalTo(587));
  }

  @Test
  @DisplayName("Should return error when tenant_id is blank")
  public void testCreateEmailConfigBlankTenantId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put("tenant_id", "");

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("tenant_id cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when tenant_id exceeds 10 characters")
  public void testCreateEmailConfigTenantIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put("tenant_id", RandomStringUtils.randomAlphanumeric(11));

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant_id cannot exceed 10 characters"));
  }

  @Test
  @DisplayName("Should return error when host is blank")
  public void testCreateEmailConfigBlankHost() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put("host", "");

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("host cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when host exceeds 256 characters")
  public void testCreateEmailConfigHostTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longHost = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put("host", longHost);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("host cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when port is less than 1")
  public void testCreateEmailConfigPortTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put("port", 0);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("port must be between 1 and 65535"));
  }

  @Test
  @DisplayName("Should return error when port exceeds 65535")
  public void testCreateEmailConfigPortTooHigh() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put("port", 65536);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("port must be between 1 and 65535"));
  }

  @Test
  @DisplayName("Should return error when send_email_path is blank")
  public void testCreateEmailConfigBlankSendEmailPath() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put("send_email_path", "");

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("send_email_path cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when send_email_path exceeds 256 characters")
  public void testCreateEmailConfigSendEmailPathTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longPath = "/" + RandomStringUtils.randomAlphanumeric(256);
    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put("send_email_path", longPath);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("send_email_path cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when template_name is blank")
  public void testCreateEmailConfigBlankTemplateName() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put("template_name", "");

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("template_name cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when template_name exceeds 256 characters")
  public void testCreateEmailConfigTemplateNameTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longTemplateName = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put("template_name", longTemplateName);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("template_name cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when template_params is null")
  public void testCreateEmailConfigNullTemplateParams() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    requestBody.put("template_params", null);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("template_params cannot be null"));
  }

  @Test
  @DisplayName("Should return error when tenant-id header doesn't match body tenant_id")
  public void testCreateEmailConfigHeaderMismatch() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createEmailConfigBody();
    String differentTenantId = "diff" + RandomStringUtils.randomAlphanumeric(6);
    requestBody.put("tenant_id", differentTenantId);

    Response response = createEmailConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant-id header must match tenant_id in request body"));
  }

  @Test
  @DisplayName("Should return error when email_config already exists")
  public void testCreateEmailConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Response response = createEmailConfig(testTenantId, createEmailConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("email_config_already_exists"));
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
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean("is_ssl_enabled"), equalTo(false));
    assertThat(response.jsonPath().getString("host"), equalTo("smtp.example.com"));
    assertThat(response.jsonPath().getInt("port"), equalTo(587));
    assertThat(response.jsonPath().getString("send_email_path"), equalTo("/send"));
    assertThat(response.jsonPath().getString("template_name"), equalTo("welcome"));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for get")
  public void testGetEmailConfigMissingHeader() {
    Response response = given().get("/v1/admin/config/email-config");
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 404 when email_config not found")
  public void testGetEmailConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getEmailConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("email_config_not_found"));
  }

  @Test
  @DisplayName("Should update email_config successfully with single field")
  public void testUpdateEmailConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("host", "updated-smtp.example.com");

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("host"), equalTo("updated-smtp.example.com"));
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getEmailConfig(testTenantId);
    assertThat(dbConfig.getString("host"), equalTo("updated-smtp.example.com"));
  }

  @Test
  @DisplayName("Should update email_config successfully with multiple fields")
  public void testUpdateEmailConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("is_ssl_enabled", true);
    updateBody.put("host", "secure-smtp.example.com");
    updateBody.put("port", 465);
    updateBody.put("send_email_path", "/api/send");
    updateBody.put("template_name", "updated-welcome");

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
    assertThat(dbConfig.getString("send_email_path"), equalTo("/api/send"));
    assertThat(dbConfig.getString("template_name"), equalTo("updated-welcome"));
  }

  @Test
  @DisplayName("Should update email_config partially - only provided fields")
  public void testUpdateEmailConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createEmailConfig(testTenantId, createEmailConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("port", 2525);

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("port"), equalTo(2525));
    assertThat(response.jsonPath().getString("host"), equalTo("smtp.example.com"));
    assertThat(response.jsonPath().getBoolean("is_ssl_enabled"), equalTo(false));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for update")
  public void testUpdateEmailConfigMissingHeader() {
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("host", "updated-smtp.example.com");

    Response response =
        given()
            .header("Content-Type", "application/json")
            .body(updateBody)
            .patch("/v1/admin/config/email-config");

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
        .body(CODE, equalTo("no_fields_to_update"));
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
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("host cannot be blank"));
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
        equalTo("host cannot exceed 256 characters"));
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
        equalTo("port must be between 1 and 65535"));
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
        equalTo("port must be between 1 and 65535"));
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
        equalTo("send_email_path cannot be blank"));
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
        equalTo("send_email_path cannot exceed 256 characters"));
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
        equalTo("template_name cannot be blank"));
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
        equalTo("template_name cannot exceed 256 characters"));
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
    updateBody.put("template_params", newTemplateParams);

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    Map<String, String> templateParams = response.jsonPath().getMap("template_params");
    assertThat(templateParams.get("key1"), equalTo("value1"));
    assertThat(templateParams.get("key2"), equalTo("value2"));
  }

  @Test
  @DisplayName("Should return 404 when email_config not found for update")
  public void testUpdateEmailConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("host", "updated-smtp.example.com");

    Response response = updateEmailConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("email_config_not_found"));
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
    Response response = given().delete("/v1/admin/config/email-config");
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 404 when email_config not found for delete")
  public void testDeleteEmailConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteEmailConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("email_config_not_found"));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put("id", testTenantId);
    tenantBody.put("name", testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createEmailConfigBody() {
    Map<String, Object> emailConfigBody = new HashMap<>();
    emailConfigBody.put("tenant_id", testTenantId);
    emailConfigBody.put("is_ssl_enabled", false);
    emailConfigBody.put("host", "smtp.example.com");
    emailConfigBody.put("port", 587);
    emailConfigBody.put("send_email_path", "/send");
    emailConfigBody.put("template_name", "welcome");
    Map<String, String> templateParams = new HashMap<>();
    templateParams.put("name", "John");
    templateParams.put("email", "john@example.com");
    emailConfigBody.put("template_params", templateParams);
    return emailConfigBody;
  }
}
