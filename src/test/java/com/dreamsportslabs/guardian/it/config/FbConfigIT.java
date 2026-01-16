package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_FB_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_FB_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_APP_ID_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_APP_ID_CANNOT_EXCEED_256;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_APP_SECRET_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_APP_SECRET_CANNOT_EXCEED_256;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_APP_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_APP_SECRET;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_NAME;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_SEND_APP_SECRET;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createFbConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteFbConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getFbConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateFbConfig;
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
public class FbConfigIT {

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
  @DisplayName("Should create fb_config successfully")
  public void testCreateFbConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createFbConfig(testTenantId, createFbConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_APP_ID), equalTo("123456789"));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_APP_SECRET), equalTo("secret123"));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_SEND_APP_SECRET), equalTo(true));

    JsonObject dbConfig = DbUtils.getFbConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(dbConfig.getString(REQUEST_FIELD_APP_ID), equalTo("123456789"));
    assertThat(dbConfig.getString(REQUEST_FIELD_APP_SECRET), equalTo("secret123"));
    assertThat(dbConfig.getBoolean(REQUEST_FIELD_SEND_APP_SECRET), equalTo(true));
  }

  @Test
  @DisplayName("Should create fb_config with default send_app_secret when not provided")
  public void testCreateFbConfigWithDefaultSendAppSecret() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createFbConfigBody();
    requestBody.remove(REQUEST_FIELD_SEND_APP_SECRET);

    Response response = createFbConfig(testTenantId, requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_SEND_APP_SECRET), equalTo(true));
  }

  @Test
  @DisplayName("Should return error when app_id is blank")
  public void testCreateFbConfigBlankAppId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createFbConfigBody();
    requestBody.put(REQUEST_FIELD_APP_ID, "");

    Response response = createFbConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_APP_ID_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when app_id exceeds 256 characters")
  public void testCreateFbConfigAppIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longAppId = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createFbConfigBody();
    requestBody.put(REQUEST_FIELD_APP_ID, longAppId);

    Response response = createFbConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_APP_ID_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when app_secret is blank")
  public void testCreateFbConfigBlankAppSecret() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createFbConfigBody();
    requestBody.put(REQUEST_FIELD_APP_SECRET, "");

    Response response = createFbConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_APP_SECRET_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when app_secret exceeds 256 characters")
  public void testCreateFbConfigAppSecretTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longAppSecret = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = createFbConfigBody();
    requestBody.put(REQUEST_FIELD_APP_SECRET, longAppSecret);

    Response response = createFbConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_APP_SECRET_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when fb_config already exists")
  public void testCreateFbConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createFbConfig(testTenantId, createFbConfigBody()).then().statusCode(SC_CREATED);

    Response response = createFbConfig(testTenantId, createFbConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_FB_CONFIG_ALREADY_EXISTS));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("FB config already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should get fb_config successfully")
  public void testGetFbConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createFbConfig(testTenantId, createFbConfigBody()).then().statusCode(SC_CREATED);

    Response response = getFbConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_APP_ID), equalTo("123456789"));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_APP_SECRET), equalTo("secret123"));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_SEND_APP_SECRET), equalTo(true));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for get")
  public void testGetFbConfigMissingHeader() {
    Response response = given().get("/v1/admin/config/fb");
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 404 when fb_config not found")
  public void testGetFbConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getFbConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo(ERROR_CODE_FB_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should update fb_config successfully with single field")
  public void testUpdateFbConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createFbConfig(testTenantId, createFbConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_APP_ID, "987654321");

    Response response = updateFbConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString(REQUEST_FIELD_APP_ID), equalTo("987654321"));
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getFbConfig(testTenantId);
    assertThat(dbConfig.getString(REQUEST_FIELD_APP_ID), equalTo("987654321"));
  }

  @Test
  @DisplayName("Should update fb_config successfully with multiple fields")
  public void testUpdateFbConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createFbConfig(testTenantId, createFbConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_APP_ID, "987654321");
    updateBody.put(REQUEST_FIELD_APP_SECRET, "newsecret456");
    updateBody.put(REQUEST_FIELD_SEND_APP_SECRET, false);

    Response response = updateFbConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString(REQUEST_FIELD_APP_ID), equalTo("987654321"));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_APP_SECRET), equalTo("newsecret456"));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_SEND_APP_SECRET), equalTo(false));

    JsonObject dbConfig = DbUtils.getFbConfig(testTenantId);
    assertThat(dbConfig.getString(REQUEST_FIELD_APP_ID), equalTo("987654321"));
    assertThat(dbConfig.getString(REQUEST_FIELD_APP_SECRET), equalTo("newsecret456"));
    assertThat(dbConfig.getBoolean(REQUEST_FIELD_SEND_APP_SECRET), equalTo(false));
  }

  @Test
  @DisplayName("Should update fb_config partially - only provided fields")
  public void testUpdateFbConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createFbConfig(testTenantId, createFbConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_SEND_APP_SECRET, false);

    Response response = updateFbConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_SEND_APP_SECRET), equalTo(false));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_APP_ID), equalTo("123456789"));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_APP_SECRET), equalTo("secret123"));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for update")
  public void testUpdateFbConfigMissingHeader() {
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_APP_ID, "987654321");

    Response response =
        given()
            .header("Content-Type", "application/json")
            .body(updateBody)
            .patch("/v1/admin/config/fb");

    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateFbConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createFbConfig(testTenantId, createFbConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateFbConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
  }

  @Test
  @DisplayName("Should return error when app_id is blank in update")
  public void testUpdateFbConfigBlankAppId() {
    createTenant(createTenantBody()).then().statusCode(201);
    createFbConfig(testTenantId, createFbConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_APP_ID, "");

    Response response = updateFbConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_APP_ID_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when app_id exceeds 256 characters in update")
  public void testUpdateFbConfigAppIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createFbConfig(testTenantId, createFbConfigBody()).then().statusCode(SC_CREATED);

    String longAppId = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_APP_ID, longAppId);

    Response response = updateFbConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_APP_ID_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when app_secret is blank in update")
  public void testUpdateFbConfigBlankAppSecret() {
    createTenant(createTenantBody()).then().statusCode(201);
    createFbConfig(testTenantId, createFbConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_APP_SECRET, "");

    Response response = updateFbConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_APP_SECRET_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when app_secret exceeds 256 characters in update")
  public void testUpdateFbConfigAppSecretTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);
    createFbConfig(testTenantId, createFbConfigBody()).then().statusCode(SC_CREATED);

    String longAppSecret = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_APP_SECRET, longAppSecret);

    Response response = updateFbConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_APP_SECRET_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return 404 when fb_config not found for update")
  public void testUpdateFbConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_APP_ID, "987654321");

    Response response = updateFbConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo(ERROR_CODE_FB_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should delete fb_config successfully")
  public void testDeleteFbConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createFbConfig(testTenantId, createFbConfigBody()).then().statusCode(SC_CREATED);

    Response response = deleteFbConfig(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getFbConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for delete")
  public void testDeleteFbConfigMissingHeader() {
    Response response = given().delete("/v1/admin/config/fb");
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 404 when fb_config not found for delete")
  public void testDeleteFbConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteFbConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo(ERROR_CODE_FB_CONFIG_NOT_FOUND));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put(REQUEST_FIELD_ID, testTenantId);
    tenantBody.put(REQUEST_FIELD_NAME, testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createFbConfigBody() {
    Map<String, Object> fbConfigBody = new HashMap<>();
    fbConfigBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    fbConfigBody.put(REQUEST_FIELD_APP_ID, "123456789");
    fbConfigBody.put(REQUEST_FIELD_APP_SECRET, "secret123");
    fbConfigBody.put(REQUEST_FIELD_SEND_APP_SECRET, true);
    return fbConfigBody;
  }
}
