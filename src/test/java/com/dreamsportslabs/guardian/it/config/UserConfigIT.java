package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_HOST_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_PORT_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_PORT_MUST_BE_LESS_THAN_OR_EQUAL_TO_65535;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_HOST;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_NAME;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_PORT;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_HOST;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getUserConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateUserConfig;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupChangelog;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
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
public class UserConfigIT {

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
  @DisplayName("Should get user_config successfully")
  public void testGetUserConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getUserConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean("is_ssl_enabled"), equalTo(false));
    assertThat(response.jsonPath().getString("host"), equalTo(DEFAULT_USER_CONFIG_HOST));
    assertThat(response.jsonPath().getInt("port"), equalTo(80));
    assertThat(response.jsonPath().getString("get_user_path"), equalTo("/users/validate"));
    assertThat(response.jsonPath().getString("create_user_path"), equalTo("/users"));
    assertThat(
        response.jsonPath().getString("authenticate_user_path"), equalTo("/api/user/validate"));
    assertThat(response.jsonPath().getString("add_provider_path"), equalTo(""));
    assertThat(response.jsonPath().getBoolean("send_provider_details"), equalTo(false));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing")
  public void testGetUserConfigMissingHeader() {
    Response response = given().get("/v1/admin/config/user");
    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should update user_config successfully with single field")
  public void testUpdateUserConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_HOST, "updated-host.dream11.local");

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("host"), equalTo("updated-host.dream11.local"));
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getUserConfig(testTenantId);
    assertThat(dbConfig.getString("host"), equalTo("updated-host.dream11.local"));
  }

  @Test
  @DisplayName("Should update user_config successfully with multiple fields")
  public void testUpdateUserConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("is_ssl_enabled", true);
    updateBody.put(REQUEST_FIELD_HOST, "secure-host.dream11.local");
    updateBody.put(REQUEST_FIELD_PORT, 443);
    updateBody.put("get_user_path", "/api/users/validate");
    updateBody.put("send_provider_details", true);

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_ssl_enabled"), equalTo(true));
    assertThat(response.jsonPath().getString("host"), equalTo("secure-host.dream11.local"));
    assertThat(response.jsonPath().getInt("port"), equalTo(443));
    assertThat(response.jsonPath().getString("get_user_path"), equalTo("/api/users/validate"));
    assertThat(response.jsonPath().getBoolean("send_provider_details"), equalTo(true));

    JsonObject dbConfig = DbUtils.getUserConfig(testTenantId);
    assertThat(dbConfig.getBoolean("is_ssl_enabled"), equalTo(true));
    assertThat(dbConfig.getString("host"), equalTo("secure-host.dream11.local"));
    assertThat(dbConfig.getInteger("port"), equalTo(443));
    assertThat(dbConfig.getString("get_user_path"), equalTo("/api/users/validate"));
    assertThat(dbConfig.getBoolean("send_provider_details"), equalTo(true));
  }

  @Test
  @DisplayName("Should update user_config partially - only provided fields")
  public void testUpdateUserConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_PORT, 8080);

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("port"), equalTo(8080));
    assertThat(response.jsonPath().getString("host"), equalTo(DEFAULT_USER_CONFIG_HOST));
    assertThat(response.jsonPath().getBoolean("is_ssl_enabled"), equalTo(false));
  }

  @Test
  @DisplayName("Should return 401 when tenant-id header is missing for update")
  public void testUpdateUserConfigMissingHeader() {
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_HOST, "updated-host.dream11.local");

    Response response =
        given()
            .header("Content-Type", "application/json")
            .body(updateBody)
            .patch("/v1/admin/config/user");

    response.then().statusCode(SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateUserConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateUserConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
  }

  @Test
  @DisplayName("Should return error when host is blank")
  public void testUpdateUserConfigBlankHost() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_HOST, "");

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_HOST_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when host exceeds 256 characters")
  public void testUpdateUserConfigHostTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longHost = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_HOST, longHost);

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("host cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when port is less than 1")
  public void testUpdateUserConfigPortTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_PORT, 0);

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PORT_MUST_BE_GREATER_THAN_OR_EQUAL_TO_1));
  }

  @Test
  @DisplayName("Should return error when port exceeds 65535")
  public void testUpdateUserConfigPortTooHigh() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_PORT, 65536);

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PORT_MUST_BE_LESS_THAN_OR_EQUAL_TO_65535));
  }

  @Test
  @DisplayName("Should return error when get_user_path is blank")
  public void testUpdateUserConfigBlankGetUserPath() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("get_user_path", "");

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("get_user_path cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when get_user_path exceeds 256 characters")
  public void testUpdateUserConfigGetUserPathTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    String longPath = "/" + RandomStringUtils.randomAlphanumeric(256);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("get_user_path", longPath);

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("get_user_path cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when create_user_path is blank")
  public void testUpdateUserConfigBlankCreateUserPath() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("create_user_path", "");

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("create_user_path cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when authenticate_user_path is blank")
  public void testUpdateUserConfigBlankAuthenticateUserPath() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("authenticate_user_path", "");

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("authenticate_user_path cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when add_provider_path is blank")
  public void testUpdateUserConfigBlankAddProviderPath() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("add_provider_path", "");

    Response response = updateUserConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("add_provider_path cannot be blank"));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put(REQUEST_FIELD_ID, testTenantId);
    tenantBody.put(REQUEST_FIELD_NAME, testTenantName);
    return tenantBody;
  }
}
