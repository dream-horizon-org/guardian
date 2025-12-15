package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createContactVerifyConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteContactVerifyConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getContactVerifyConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateContactVerifyConfig;
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

public class ContactVerifyConfigIT {

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
  @DisplayName("Should create contact_verify_config successfully")
  public void testCreateContactVerifyConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createContactVerifyConfig(testTenantId, createContactVerifyConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getBoolean("is_otp_mocked"), equalTo(false));
    assertThat(response.jsonPath().getInt("otp_length"), equalTo(6));
    assertThat(response.jsonPath().getInt("try_limit"), equalTo(5));
    assertThat(response.jsonPath().getInt("resend_limit"), equalTo(5));
    assertThat(response.jsonPath().getInt("otp_resend_interval"), equalTo(30));
    assertThat(response.jsonPath().getInt("otp_validity"), equalTo(900));

    JsonObject dbConfig = DbUtils.getContactVerifyConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString("tenant_id"), equalTo(testTenantId));
    assertThat(dbConfig.getBoolean("is_otp_mocked"), equalTo(false));
    assertThat(dbConfig.getInteger("otp_length"), equalTo(6));
  }

  @Test
  @DisplayName("Should create contact_verify_config with default values when not provided")
  public void testCreateContactVerifyConfigWithDefaults() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tenant_id", testTenantId);
    Map<String, Boolean> whitelistedInputs = new HashMap<>();
    whitelistedInputs.put("email", true);
    whitelistedInputs.put("mobile", true);
    requestBody.put("whitelisted_inputs", whitelistedInputs);

    Response response = createContactVerifyConfig(testTenantId, requestBody);

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
  public void testCreateContactVerifyConfigBlankTenantId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createContactVerifyConfigBody();
    requestBody.put("tenant_id", "");

    Response response = createContactVerifyConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("tenant_id cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when tenant_id exceeds 10 characters")
  public void testCreateContactVerifyConfigTenantIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createContactVerifyConfigBody();
    requestBody.put("tenant_id", RandomStringUtils.randomAlphanumeric(11));

    Response response = createContactVerifyConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant_id cannot exceed 10 characters"));
  }

  @Test
  @DisplayName("Should return error when otp_length is less than 1")
  public void testCreateContactVerifyConfigOtpLengthTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createContactVerifyConfigBody();
    requestBody.put("otp_length", 0);

    Response response = createContactVerifyConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_length must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when try_limit is less than 1")
  public void testCreateContactVerifyConfigTryLimitTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createContactVerifyConfigBody();
    requestBody.put("try_limit", 0);

    Response response = createContactVerifyConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("try_limit must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when resend_limit is less than 1")
  public void testCreateContactVerifyConfigResendLimitTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createContactVerifyConfigBody();
    requestBody.put("resend_limit", 0);

    Response response = createContactVerifyConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("resend_limit must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when otp_resend_interval is less than 1")
  public void testCreateContactVerifyConfigOtpResendIntervalTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createContactVerifyConfigBody();
    requestBody.put("otp_resend_interval", 0);

    Response response = createContactVerifyConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_resend_interval must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when otp_validity is less than 1")
  public void testCreateContactVerifyConfigOtpValidityTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createContactVerifyConfigBody();
    requestBody.put("otp_validity", 0);

    Response response = createContactVerifyConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_validity must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when whitelisted_inputs is null")
  public void testCreateContactVerifyConfigNullWhitelistedInputs() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createContactVerifyConfigBody();
    requestBody.put("whitelisted_inputs", null);

    Response response = createContactVerifyConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("whitelisted_inputs cannot be null"));
  }

  @Test
  @DisplayName("Should return error when tenant-id header doesn't match body tenant_id")
  public void testCreateContactVerifyConfigHeaderMismatch() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createContactVerifyConfigBody();
    String differentTenantId = "diff" + RandomStringUtils.randomAlphanumeric(6);
    requestBody.put("tenant_id", differentTenantId);

    Response response = createContactVerifyConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant-id header must match tenant_id in request body"));
  }

  @Test
  @DisplayName("Should return error when contact_verify_config already exists")
  public void testCreateContactVerifyConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Response response = createContactVerifyConfig(testTenantId, createContactVerifyConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo("contact_verify_config_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("Contact verify config already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should get contact_verify_config successfully")
  public void testGetContactVerifyConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Response response = getContactVerifyConfig(testTenantId);

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
  @DisplayName("Should return 404 when contact_verify_config not found")
  public void testGetContactVerifyConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getContactVerifyConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo("contact_verify_config_not_found"));
  }

  @Test
  @DisplayName("Should update contact_verify_config successfully with single field")
  public void testUpdateContactVerifyConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("otp_length", 8);

    Response response = updateContactVerifyConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("otp_length"), equalTo(8));
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getContactVerifyConfig(testTenantId);
    assertThat(dbConfig.getInteger("otp_length"), equalTo(8));
  }

  @Test
  @DisplayName("Should update contact_verify_config successfully with multiple fields")
  public void testUpdateContactVerifyConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("is_otp_mocked", true);
    updateBody.put("otp_length", 8);
    updateBody.put("try_limit", 10);
    updateBody.put("resend_limit", 10);
    updateBody.put("otp_resend_interval", 60);
    updateBody.put("otp_validity", 1800);

    Response response = updateContactVerifyConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_otp_mocked"), equalTo(true));
    assertThat(response.jsonPath().getInt("otp_length"), equalTo(8));
    assertThat(response.jsonPath().getInt("try_limit"), equalTo(10));
    assertThat(response.jsonPath().getInt("resend_limit"), equalTo(10));
    assertThat(response.jsonPath().getInt("otp_resend_interval"), equalTo(60));
    assertThat(response.jsonPath().getInt("otp_validity"), equalTo(1800));

    JsonObject dbConfig = DbUtils.getContactVerifyConfig(testTenantId);
    assertThat(dbConfig.getBoolean("is_otp_mocked"), equalTo(true));
    assertThat(dbConfig.getInteger("otp_length"), equalTo(8));
    assertThat(dbConfig.getInteger("try_limit"), equalTo(10));
  }

  @Test
  @DisplayName("Should update contact_verify_config partially - only provided fields")
  public void testUpdateContactVerifyConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("is_otp_mocked", true);

    Response response = updateContactVerifyConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_otp_mocked"), equalTo(true));
    assertThat(response.jsonPath().getInt("otp_length"), equalTo(6));
    assertThat(response.jsonPath().getInt("try_limit"), equalTo(5));
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateContactVerifyConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateContactVerifyConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("no_fields_to_update"));
  }

  @Test
  @DisplayName("Should return error when otp_length is less than 1 in update")
  public void testUpdateContactVerifyConfigOtpLengthTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("otp_length", 0);

    Response response = updateContactVerifyConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_length must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when try_limit is less than 1 in update")
  public void testUpdateContactVerifyConfigTryLimitTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("try_limit", 0);

    Response response = updateContactVerifyConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("try_limit must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when resend_limit is less than 1 in update")
  public void testUpdateContactVerifyConfigResendLimitTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("resend_limit", 0);

    Response response = updateContactVerifyConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("resend_limit must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when otp_resend_interval is less than 1 in update")
  public void testUpdateContactVerifyConfigOtpResendIntervalTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("otp_resend_interval", 0);

    Response response = updateContactVerifyConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_resend_interval must be greater than 0"));
  }

  @Test
  @DisplayName("Should return error when otp_validity is less than 1 in update")
  public void testUpdateContactVerifyConfigOtpValidityTooLow() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("otp_validity", 0);

    Response response = updateContactVerifyConfig(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("otp_validity must be greater than 0"));
  }

  @Test
  @DisplayName("Should update whitelisted_inputs successfully")
  public void testUpdateContactVerifyConfigWhitelistedInputs() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Boolean> newWhitelistedInputs = new HashMap<>();
    newWhitelistedInputs.put("email", true);
    newWhitelistedInputs.put("mobile", true);
    newWhitelistedInputs.put("username", true);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("whitelisted_inputs", newWhitelistedInputs);

    Response response = updateContactVerifyConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    Map<String, Object> whitelistedInputsMap = response.jsonPath().getMap("whitelisted_inputs");
    assertThat(whitelistedInputsMap.size(), equalTo(3));
    assertThat(whitelistedInputsMap.containsKey("email"), equalTo(true));
    assertThat(whitelistedInputsMap.containsKey("mobile"), equalTo(true));
    assertThat(whitelistedInputsMap.containsKey("username"), equalTo(true));
  }

  @Test
  @DisplayName("Should return 404 when contact_verify_config not found for update")
  public void testUpdateContactVerifyConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("otp_length", 8);

    Response response = updateContactVerifyConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo("contact_verify_config_not_found"));
  }

  @Test
  @DisplayName("Should delete contact_verify_config successfully")
  public void testDeleteContactVerifyConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createContactVerifyConfig(testTenantId, createContactVerifyConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Response response = deleteContactVerifyConfig(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getContactVerifyConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 404 when contact_verify_config not found for delete")
  public void testDeleteContactVerifyConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteContactVerifyConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo("contact_verify_config_not_found"));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put("id", testTenantId);
    tenantBody.put("name", testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createContactVerifyConfigBody() {
    Map<String, Object> contactVerifyConfigBody = new HashMap<>();
    contactVerifyConfigBody.put("tenant_id", testTenantId);
    contactVerifyConfigBody.put("is_otp_mocked", false);
    contactVerifyConfigBody.put("otp_length", 6);
    contactVerifyConfigBody.put("try_limit", 5);
    contactVerifyConfigBody.put("resend_limit", 5);
    contactVerifyConfigBody.put("otp_resend_interval", 30);
    contactVerifyConfigBody.put("otp_validity", 900);
    Map<String, Boolean> whitelistedInputs = new HashMap<>();
    whitelistedInputs.put("email", true);
    whitelistedInputs.put("mobile", true);
    contactVerifyConfigBody.put("whitelisted_inputs", whitelistedInputs);
    return contactVerifyConfigBody;
  }
}
