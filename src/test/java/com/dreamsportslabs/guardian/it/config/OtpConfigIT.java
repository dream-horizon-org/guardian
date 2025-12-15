package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createOtpConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteOtpConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getOtpConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateOtpConfig;
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

public class OtpConfigIT {

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
  @DisplayName("Should create otp_config successfully")
  public void testCreateOtpConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createOtpConfig(testTenantId, createOtpConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean("is_otp_mocked"), equalTo(false));
    assertThat(response.jsonPath().getInt("otp_length"), equalTo(6));
    assertThat(response.jsonPath().getInt("try_limit"), equalTo(5));
    assertThat(response.jsonPath().getInt("resend_limit"), equalTo(5));
    assertThat(response.jsonPath().getInt("otp_resend_interval"), equalTo(30));
    assertThat(response.jsonPath().getInt("otp_validity"), equalTo(900));

    JsonObject dbConfig = DbUtils.getOtpConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString("tenant_id"), equalTo(testTenantId));
    assertThat(dbConfig.getBoolean("is_otp_mocked"), equalTo(false));
    assertThat(dbConfig.getInteger("otp_length"), equalTo(6));
  }

  @Test
  @DisplayName("Should create otp_config with default values when not provided")
  public void testCreateOtpConfigWithDefaults() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tenant_id", testTenantId);
    requestBody.put("whitelisted_inputs", List.of("email", "mobile"));

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getBoolean("is_otp_mocked"), equalTo(false));
    assertThat(response.jsonPath().getInt("otp_length"), equalTo(6));
    assertThat(response.jsonPath().getInt("try_limit"), equalTo(5));
    assertThat(response.jsonPath().getInt("resend_limit"), equalTo(5));
    assertThat(response.jsonPath().getInt("otp_resend_interval"), equalTo(30));
    assertThat(response.jsonPath().getInt("otp_validity"), equalTo(900));
  }

  @Test
  @DisplayName("Should return error when tenant_id is blank")
  public void testCreateOtpConfigBlankTenantId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put("tenant_id", "");

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("tenant_id cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when tenant_id exceeds 10 characters")
  public void testCreateOtpConfigTenantIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put("tenant_id", RandomStringUtils.randomAlphanumeric(11));

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant_id cannot exceed 10 characters"));
  }

  @Test
  @DisplayName("Should return error when otp_length is less than 1")
  public void testCreateOtpConfigOtpLengthTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put("otp_length", 0);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_length must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when try_limit is less than 1")
  public void testCreateOtpConfigTryLimitTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put("try_limit", 0);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("try_limit must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when resend_limit is less than 1")
  public void testCreateOtpConfigResendLimitTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put("resend_limit", 0);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("resend_limit must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when otp_resend_interval is less than 1")
  public void testCreateOtpConfigOtpResendIntervalTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put("otp_resend_interval", 0);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_resend_interval must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when otp_validity is less than 1")
  public void testCreateOtpConfigOtpValidityTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put("otp_validity", 0);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_validity must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when whitelisted_inputs is null")
  public void testCreateOtpConfigNullWhitelistedInputs() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put("whitelisted_inputs", null);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("whitelisted_inputs cannot be null"));
  }

  @Test
  @DisplayName("Should return error when tenant-id header doesn't match body tenant_id")
  public void testCreateOtpConfigHeaderMismatch() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    String differentTenantId = "diff" + RandomStringUtils.randomAlphanumeric(6);
    requestBody.put("tenant_id", differentTenantId);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant-id header must match tenant_id in request body"));
  }

  @Test
  @DisplayName("Should return error when otp_config already exists")
  public void testCreateOtpConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Response response = createOtpConfig(testTenantId, createOtpConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("otp_config_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("OTP config already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should get otp_config successfully")
  public void testGetOtpConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Response response = getOtpConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean("is_otp_mocked"), equalTo(false));
    assertThat(response.jsonPath().getInt("otp_length"), equalTo(6));
    assertThat(response.jsonPath().getInt("try_limit"), equalTo(5));
    assertThat(response.jsonPath().getInt("resend_limit"), equalTo(5));
    assertThat(response.jsonPath().getInt("otp_resend_interval"), equalTo(30));
    assertThat(response.jsonPath().getInt("otp_validity"), equalTo(900));
  }

  @Test
  @DisplayName("Should return 404 when otp_config not found")
  public void testGetOtpConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getOtpConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(response.jsonPath().getString(ERROR + "." + CODE), equalTo("otp_config_not_found"));
  }

  @Test
  @DisplayName("Should update otp_config successfully with single field")
  public void testUpdateOtpConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("otp_length", 8);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("otp_length"), equalTo(8));
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getOtpConfig(testTenantId);
    assertThat(dbConfig.getInteger("otp_length"), equalTo(8));
  }

  @Test
  @DisplayName("Should update otp_config successfully with multiple fields")
  public void testUpdateOtpConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("is_otp_mocked", true);
    updateBody.put("otp_length", 8);
    updateBody.put("try_limit", 10);
    updateBody.put("resend_limit", 10);
    updateBody.put("otp_resend_interval", 60);
    updateBody.put("otp_validity", 1800);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_otp_mocked"), equalTo(true));
    assertThat(response.jsonPath().getInt("otp_length"), equalTo(8));
    assertThat(response.jsonPath().getInt("try_limit"), equalTo(10));
    assertThat(response.jsonPath().getInt("resend_limit"), equalTo(10));
    assertThat(response.jsonPath().getInt("otp_resend_interval"), equalTo(60));
    assertThat(response.jsonPath().getInt("otp_validity"), equalTo(1800));

    JsonObject dbConfig = DbUtils.getOtpConfig(testTenantId);
    assertThat(dbConfig.getBoolean("is_otp_mocked"), equalTo(true));
    assertThat(dbConfig.getInteger("otp_length"), equalTo(8));
    assertThat(dbConfig.getInteger("try_limit"), equalTo(10));
  }

  @Test
  @DisplayName("Should update otp_config partially - only provided fields")
  public void testUpdateOtpConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("is_otp_mocked", true);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_otp_mocked"), equalTo(true));
    assertThat(response.jsonPath().getInt("otp_length"), equalTo(6));
    assertThat(response.jsonPath().getInt("try_limit"), equalTo(5));
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateOtpConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateOtpConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("no_fields_to_update"));
  }

  @Test
  @DisplayName("Should return error when otp_length is less than 1 in update")
  public void testUpdateOtpConfigOtpLengthTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("otp_length", 0);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_length must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when try_limit is less than 1 in update")
  public void testUpdateOtpConfigTryLimitTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("try_limit", 0);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("try_limit must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when resend_limit is less than 1 in update")
  public void testUpdateOtpConfigResendLimitTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("resend_limit", 0);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("resend_limit must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when otp_resend_interval is less than 1 in update")
  public void testUpdateOtpConfigOtpResendIntervalTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("otp_resend_interval", 0);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_resend_interval must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when otp_validity is less than 1 in update")
  public void testUpdateOtpConfigOtpValidityTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("otp_validity", 0);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_validity must be greater than 0"));
  }

  @Test
  @DisplayName("Should update whitelisted_inputs successfully")
  public void testUpdateOtpConfigWhitelistedInputs() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    List<String> newWhitelistedInputs = new ArrayList<>();
    newWhitelistedInputs.add("email");
    newWhitelistedInputs.add("mobile");
    newWhitelistedInputs.add("username");

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("whitelisted_inputs", newWhitelistedInputs);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    List<String> whitelistedInputs = response.jsonPath().getList("whitelisted_inputs");
    assertThat(whitelistedInputs.size(), equalTo(3));
    assertThat(whitelistedInputs.contains("email"), equalTo(true));
    assertThat(whitelistedInputs.contains("mobile"), equalTo(true));
    assertThat(whitelistedInputs.contains("username"), equalTo(true));
  }

  @Test
  @DisplayName("Should return 404 when otp_config not found for update")
  public void testUpdateOtpConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("otp_length", 8);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(response.jsonPath().getString(ERROR + "." + CODE), equalTo("otp_config_not_found"));
  }

  @Test
  @DisplayName("Should delete otp_config successfully")
  public void testDeleteOtpConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Response response = deleteOtpConfig(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getOtpConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 404 when otp_config not found for delete")
  public void testDeleteOtpConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteOtpConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(response.jsonPath().getString(ERROR + "." + CODE), equalTo("otp_config_not_found"));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put("id", testTenantId);
    tenantBody.put("name", testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createOtpConfigBody() {
    Map<String, Object> otpConfigBody = new HashMap<>();
    otpConfigBody.put("tenant_id", testTenantId);
    otpConfigBody.put("is_otp_mocked", false);
    otpConfigBody.put("otp_length", 6);
    otpConfigBody.put("try_limit", 5);
    otpConfigBody.put("resend_limit", 5);
    otpConfigBody.put("otp_resend_interval", 30);
    otpConfigBody.put("otp_validity", 900);
    otpConfigBody.put("whitelisted_inputs", List.of("email", "mobile"));
    return otpConfigBody;
  }
}
