package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getTenantByName;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateTenant;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupChangelog;
import static com.dreamsportslabs.guardian.utils.DbUtils.tenantExists;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dreamsportslabs.guardian.utils.DbUtils;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TenantIT {

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
  @DisplayName("Should create tenant successfully")
  public void testCreateTenantSuccess() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", testTenantId);
    requestBody.put("name", testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString("id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("name"), equalTo(testTenantName));
    assertThat(tenantExists(testTenantId), equalTo(true));
  }

  @Test
  @DisplayName("Should return error when id is missing")
  public void testCreateTenantMissingId() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("name", testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("id is required"));
  }

  @Test
  @DisplayName("Should return error when name is missing")
  public void testCreateTenantMissingName() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", testTenantId);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("name is required"));
  }

  @Test
  @DisplayName("Should return error when id is blank")
  public void testCreateTenantBlankId() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", "");
    requestBody.put("name", testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("id is required"));
  }

  @Test
  @DisplayName("Should return error when name is blank")
  public void testCreateTenantBlankName() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", testTenantId);
    requestBody.put("name", "");

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("name is required"));
  }

  @Test
  @DisplayName("Should return error when id exceeds 10 characters")
  public void testCreateTenantIdTooLong() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", "12345678901");
    requestBody.put("name", testTenantName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("id cannot exceed 10 characters"));
  }

  @Test
  @DisplayName("Should return error when name exceeds 256 characters")
  public void testCreateTenantNameTooLong() {
    String longName = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", testTenantId);
    requestBody.put("name", longName);

    Response response = createTenant(requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("name cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when tenant id already exists")
  public void testCreateTenantDuplicateId() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", testTenantId);
    requestBody.put("name", testTenantName);

    createTenant(requestBody).then().statusCode(SC_CREATED);

    Map<String, Object> duplicateRequest = new HashMap<>();
    duplicateRequest.put("id", testTenantId);
    duplicateRequest.put("name", "Another Name");

    Response response = createTenant(duplicateRequest);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("tenant_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("Tenant ID already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should return error when tenant name already exists")
  public void testCreateTenantDuplicateName() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", testTenantId);
    requestBody.put("name", testTenantName);

    createTenant(requestBody).then().statusCode(SC_CREATED);

    Map<String, Object> duplicateRequest = new HashMap<>();
    duplicateRequest.put("id", "different");
    duplicateRequest.put("name", testTenantName);

    Response response = createTenant(duplicateRequest);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("tenant_name_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("Tenant name already exists: " + testTenantName));
  }

  @Test
  @DisplayName("Should get tenant by id successfully")
  public void testGetTenantByIdSuccess() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", testTenantId);
    requestBody.put("name", testTenantName);

    createTenant(requestBody).then().statusCode(SC_CREATED);

    Response response = getTenant(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("name"), equalTo(testTenantName));
  }

  @Test
  @DisplayName("Should return 404 when tenant not found by id")
  public void testGetTenantByIdNotFound() {
    Response response = getTenant("nonexistent");

    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo("tenant_not_found"));
  }

  @Test
  @DisplayName("Should get tenant by name successfully")
  public void testGetTenantByNameSuccess() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", testTenantId);
    requestBody.put("name", testTenantName);

    createTenant(requestBody).then().statusCode(SC_CREATED);

    Response response = getTenantByName(testTenantName);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("name"), equalTo(testTenantName));
  }

  @Test
  @DisplayName("Should return 404 when tenant not found by name")
  public void testGetTenantByNameNotFound() {
    Response response = getTenantByName("Nonexistent Tenant");

    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo("tenant_not_found"));
  }

  @Test
  @DisplayName("Should update tenant successfully")
  public void testUpdateTenantSuccess() {
    Map<String, Object> createBody = new HashMap<>();
    createBody.put("id", testTenantId);
    createBody.put("name", testTenantName);

    createTenant(createBody).then().statusCode(SC_CREATED);

    String updatedName = "Updated " + testTenantName;
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("name", updatedName);

    Response response = updateTenant(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("name"), equalTo(updatedName));

    Response getResponse = getTenant(testTenantId);
    assertThat(getResponse.jsonPath().getString("name"), equalTo(updatedName));
  }

  @Test
  @DisplayName("Should return 404 when updating non-existent tenant")
  public void testUpdateTenantNotFound() {
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("name", "Updated Name");

    Response response = updateTenant("nonexistent", updateBody);

    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo("tenant_not_found"));
  }

  @Test
  @DisplayName("Should return error when update name is blank")
  public void testUpdateTenantBlankName() {
    Map<String, Object> createBody = new HashMap<>();
    createBody.put("id", testTenantId);
    createBody.put("name", testTenantName);

    createTenant(createBody).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("name", "");

    Response response = updateTenant(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("name cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when update name exceeds 256 characters")
  public void testUpdateTenantNameTooLong() {
    Map<String, Object> createBody = new HashMap<>();
    createBody.put("id", testTenantId);
    createBody.put("name", testTenantName);

    createTenant(createBody).then().statusCode(SC_CREATED);

    String longName = RandomStringUtils.randomAlphanumeric(257);
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("name", longName);

    Response response = updateTenant(testTenantId, updateBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("name cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when update has no fields")
  public void testUpdateTenantNoFields() {
    Map<String, Object> createBody = new HashMap<>();
    createBody.put("id", testTenantId);
    createBody.put("name", testTenantName);

    createTenant(createBody).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateTenant(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("no_fields_to_update"));
  }

  @Test
  @DisplayName("Should return error when updating with duplicate name")
  public void testUpdateTenantDuplicateName() {
    String tenantId2 = "test2" + RandomStringUtils.randomAlphanumeric(5);
    String tenantName2 = "Another Tenant";

    Map<String, Object> createBody1 = new HashMap<>();
    createBody1.put("id", testTenantId);
    createBody1.put("name", testTenantName);
    createTenant(createBody1).then().statusCode(SC_CREATED);

    Map<String, Object> createBody2 = new HashMap<>();
    createBody2.put("id", tenantId2);
    createBody2.put("name", tenantName2);
    createTenant(createBody2).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("name", tenantName2);

    Response response = updateTenant(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("tenant_name_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("Tenant name already exists: " + tenantName2));

    DbUtils.deleteTenant(tenantId2);
  }

  @Test
  @DisplayName("Should delete tenant successfully")
  public void testDeleteTenantSuccess() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("id", testTenantId);
    requestBody.put("name", testTenantName);

    createTenant(requestBody).then().statusCode(SC_CREATED);
    assertThat(tenantExists(testTenantId), equalTo(true));

    Response response = deleteTenant(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);
    assertThat(tenantExists(testTenantId), equalTo(false));
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent tenant")
  public void testDeleteTenantNotFound() {
    Response response = deleteTenant("nonexistent");

    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo("tenant_not_found"));
  }
}
