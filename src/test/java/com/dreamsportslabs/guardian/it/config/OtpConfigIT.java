package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_OTP_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.Constants.ERROR_CODE_OTP_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_OTP_LENGTH_MUST_BE_GREATER_THAN_0;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_OTP_RESEND_INTERVAL_MUST_BE_GREATER_THAN_0;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_OTP_VALIDITY_MUST_BE_GREATER_THAN_0;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_RESEND_LIMIT_MUST_BE_GREATER_THAN_0;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_TRY_LIMIT_MUST_BE_GREATER_THAN_0;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_WHITELISTED_INPUTS_CANNOT_BE_NULL;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_IS_OTP_MOCKED;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_NAME;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_OTP_LENGTH;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_OTP_RESEND_INTERVAL;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_OTP_VALIDITY;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_RESEND_LIMIT;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TRY_LIMIT;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_WHITELISTED_INPUTS;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
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
import java.util.HashMap;
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
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_OTP_MOCKED), equalTo(false));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_LENGTH), equalTo(6));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_TRY_LIMIT), equalTo(5));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_RESEND_LIMIT), equalTo(5));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_RESEND_INTERVAL), equalTo(30));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_VALIDITY), equalTo(900));

    JsonObject dbConfig = DbUtils.getOtpConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(dbConfig.getBoolean(REQUEST_FIELD_IS_OTP_MOCKED), equalTo(false));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_OTP_LENGTH), equalTo(6));
  }

  @Test
  @DisplayName("Should create otp_config with default values when not provided")
  public void testCreateOtpConfigWithDefaults() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    Map<String, Boolean> whitelistedInputs = new HashMap<>();
    whitelistedInputs.put("email", true);
    whitelistedInputs.put("mobile", true);
    requestBody.put(REQUEST_FIELD_WHITELISTED_INPUTS, whitelistedInputs);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_OTP_MOCKED), equalTo(false));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_LENGTH), equalTo(6));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_TRY_LIMIT), equalTo(5));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_RESEND_LIMIT), equalTo(5));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_RESEND_INTERVAL), equalTo(30));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_VALIDITY), equalTo(900));
  }

  @Test
  @DisplayName("Should return error when tenant_id is blank")
  public void testCreateOtpConfigBlankTenantId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put(REQUEST_FIELD_TENANT_ID, "");

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
    requestBody.put(REQUEST_FIELD_TENANT_ID, RandomStringUtils.randomAlphanumeric(11));

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
    requestBody.put(REQUEST_FIELD_OTP_LENGTH, 0);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_OTP_LENGTH_MUST_BE_GREATER_THAN_0));
  }

  @Test
  @DisplayName("Should return error when try_limit is less than 1")
  public void testCreateOtpConfigTryLimitTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put(REQUEST_FIELD_TRY_LIMIT, 0);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_TRY_LIMIT_MUST_BE_GREATER_THAN_0));
  }

  @Test
  @DisplayName("Should return error when resend_limit is less than 1")
  public void testCreateOtpConfigResendLimitTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put(REQUEST_FIELD_RESEND_LIMIT, 0);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_RESEND_LIMIT_MUST_BE_GREATER_THAN_0));
  }

  @Test
  @DisplayName("Should return error when otp_resend_interval is less than 1")
  public void testCreateOtpConfigOtpResendIntervalTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put(REQUEST_FIELD_OTP_RESEND_INTERVAL, 0);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_OTP_RESEND_INTERVAL_MUST_BE_GREATER_THAN_0));
  }

  @Test
  @DisplayName("Should return error when otp_validity is less than 1")
  public void testCreateOtpConfigOtpValidityTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put(REQUEST_FIELD_OTP_VALIDITY, 0);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_OTP_VALIDITY_MUST_BE_GREATER_THAN_0));
  }

  @Test
  @DisplayName("Should return error when whitelisted_inputs is null")
  public void testCreateOtpConfigNullWhitelistedInputs() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    requestBody.put(REQUEST_FIELD_WHITELISTED_INPUTS, null);

    Response response = createOtpConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_WHITELISTED_INPUTS_CANNOT_BE_NULL));
  }

  @Test
  @DisplayName("Should return error when tenant-id header doesn't match body tenant_id")
  public void testCreateOtpConfigHeaderMismatch() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOtpConfigBody();
    String differentTenantId = "diff" + RandomStringUtils.randomAlphanumeric(6);
    requestBody.put(REQUEST_FIELD_TENANT_ID, differentTenantId);

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
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_OTP_CONFIG_ALREADY_EXISTS));
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
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_OTP_MOCKED), equalTo(false));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_LENGTH), equalTo(6));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_TRY_LIMIT), equalTo(5));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_RESEND_LIMIT), equalTo(5));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_RESEND_INTERVAL), equalTo(30));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_VALIDITY), equalTo(900));
  }

  @Test
  @DisplayName("Should return 404 when otp_config not found")
  public void testGetOtpConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getOtpConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_OTP_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should update otp_config successfully with single field")
  public void testUpdateOtpConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_OTP_LENGTH, 8);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_LENGTH), equalTo(8));
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getOtpConfig(testTenantId);
    assertThat(dbConfig.getInteger(REQUEST_FIELD_OTP_LENGTH), equalTo(8));
  }

  @Test
  @DisplayName("Should update otp_config successfully with multiple fields")
  public void testUpdateOtpConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_IS_OTP_MOCKED, true);
    updateBody.put(REQUEST_FIELD_OTP_LENGTH, 8);
    updateBody.put(REQUEST_FIELD_TRY_LIMIT, 10);
    updateBody.put(REQUEST_FIELD_RESEND_LIMIT, 10);
    updateBody.put(REQUEST_FIELD_OTP_RESEND_INTERVAL, 60);
    updateBody.put(REQUEST_FIELD_OTP_VALIDITY, 1800);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_OTP_MOCKED), equalTo(true));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_LENGTH), equalTo(8));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_TRY_LIMIT), equalTo(10));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_RESEND_LIMIT), equalTo(10));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_RESEND_INTERVAL), equalTo(60));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_VALIDITY), equalTo(1800));

    JsonObject dbConfig = DbUtils.getOtpConfig(testTenantId);
    assertThat(dbConfig.getBoolean(REQUEST_FIELD_IS_OTP_MOCKED), equalTo(true));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_OTP_LENGTH), equalTo(8));
    assertThat(dbConfig.getInteger(REQUEST_FIELD_TRY_LIMIT), equalTo(10));
  }

  @Test
  @DisplayName("Should update otp_config partially - only provided fields")
  public void testUpdateOtpConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_IS_OTP_MOCKED, true);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_OTP_MOCKED), equalTo(true));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_OTP_LENGTH), equalTo(6));
    assertThat(response.jsonPath().getInt(REQUEST_FIELD_TRY_LIMIT), equalTo(5));
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
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
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
        equalTo(ERROR_MSG_OTP_LENGTH_MUST_BE_GREATER_THAN_0));
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
        equalTo(ERROR_MSG_TRY_LIMIT_MUST_BE_GREATER_THAN_0));
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
        equalTo(ERROR_MSG_RESEND_LIMIT_MUST_BE_GREATER_THAN_0));
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
        equalTo(ERROR_MSG_OTP_RESEND_INTERVAL_MUST_BE_GREATER_THAN_0));
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
        equalTo(ERROR_MSG_OTP_VALIDITY_MUST_BE_GREATER_THAN_0));
  }

  @Test
  @DisplayName("Should update whitelisted_inputs successfully")
  public void testUpdateOtpConfigWhitelistedInputs() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOtpConfig(testTenantId, createOtpConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Boolean> newWhitelistedInputs = new HashMap<>();
    newWhitelistedInputs.put("email", true);
    newWhitelistedInputs.put("mobile", true);
    newWhitelistedInputs.put("username", true);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_WHITELISTED_INPUTS, newWhitelistedInputs);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    Map<String, Object> whitelistedInputsMap =
        response.jsonPath().getMap(REQUEST_FIELD_WHITELISTED_INPUTS);
    assertThat(whitelistedInputsMap.size(), equalTo(3));
    assertThat(whitelistedInputsMap.containsKey("email"), equalTo(true));
    assertThat(whitelistedInputsMap.containsKey("mobile"), equalTo(true));
    assertThat(whitelistedInputsMap.containsKey("username"), equalTo(true));
  }

  @Test
  @DisplayName("Should return 404 when otp_config not found for update")
  public void testUpdateOtpConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_OTP_LENGTH, 8);

    Response response = updateOtpConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_OTP_CONFIG_NOT_FOUND));
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
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_OTP_CONFIG_NOT_FOUND));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put(REQUEST_FIELD_ID, testTenantId);
    tenantBody.put(REQUEST_FIELD_NAME, testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createOtpConfigBody() {
    Map<String, Object> otpConfigBody = new HashMap<>();
    otpConfigBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    otpConfigBody.put(REQUEST_FIELD_IS_OTP_MOCKED, false);
    otpConfigBody.put(REQUEST_FIELD_OTP_LENGTH, 6);
    otpConfigBody.put(REQUEST_FIELD_TRY_LIMIT, 5);
    otpConfigBody.put(REQUEST_FIELD_RESEND_LIMIT, 5);
    otpConfigBody.put(REQUEST_FIELD_OTP_RESEND_INTERVAL, 30);
    otpConfigBody.put(REQUEST_FIELD_OTP_VALIDITY, 900);
    Map<String, Boolean> whitelistedInputs = new HashMap<>();
    whitelistedInputs.put("email", true);
    whitelistedInputs.put("mobile", true);
    otpConfigBody.put(REQUEST_FIELD_WHITELISTED_INPUTS, whitelistedInputs);
    return otpConfigBody;
  }
}
