package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.ADMIN_EMAIL;
import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.CONFIG_TYPE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.Constants.CONFIG_TYPE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.Constants.CONFIG_TYPE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.Constants.DEFAULT_LIMIT;
import static com.dreamsportslabs.guardian.Constants.DEFAULT_OFFSET;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.JSON_PATH_CHANGED_AT;
import static com.dreamsportslabs.guardian.Constants.JSON_PATH_CHANGED_BY;
import static com.dreamsportslabs.guardian.Constants.JSON_PATH_CHANGES;
import static com.dreamsportslabs.guardian.Constants.JSON_PATH_CONFIG_TYPE;
import static com.dreamsportslabs.guardian.Constants.JSON_PATH_NEW_VALUES;
import static com.dreamsportslabs.guardian.Constants.JSON_PATH_OLD_VALUES;
import static com.dreamsportslabs.guardian.Constants.JSON_PATH_OPERATION_TYPE;
import static com.dreamsportslabs.guardian.Constants.JSON_PATH_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.JSON_PATH_TOTAL;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.Constants.QUERY_PARAM_LIMIT;
import static com.dreamsportslabs.guardian.Constants.QUERY_PARAM_OFFSET;
import static com.dreamsportslabs.guardian.Constants.QUERY_PARAM_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getChangelog;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getChangelogById;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupChangelog;
import static com.dreamsportslabs.guardian.utils.DbUtils.countChangelogByTenant;
import static com.dreamsportslabs.guardian.utils.DbUtils.deleteTenant;
import static com.dreamsportslabs.guardian.utils.DbUtils.insertChangelog;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ChangelogIT {

  @BeforeEach
  void setUp() {
    cleanupChangelog(TENANT_1);
  }

  @AfterEach
  void tearDown() {
    cleanupChangelog(TENANT_1);
  }

  @Test
  @DisplayName("Should get changelog list for tenant")
  public void testGetChangelogList() {
    String oldValues = "{\"key1\":\"value1\"}";
    String newValues = "{\"key1\":\"value2\"}";

    insertChangelog(
        TENANT_1, CONFIG_TYPE_OTP_CONFIG, OPERATION_UPDATE, oldValues, newValues, ADMIN_EMAIL);
    insertChangelog(
        TENANT_1, CONFIG_TYPE_EMAIL_CONFIG, OPERATION_INSERT, null, newValues, ADMIN_EMAIL);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(QUERY_PARAM_TENANT_ID, TENANT_1);
    queryParams.put(QUERY_PARAM_LIMIT, DEFAULT_LIMIT);
    queryParams.put(QUERY_PARAM_OFFSET, DEFAULT_OFFSET);

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getLong(JSON_PATH_TOTAL), equalTo(2L));
    assertThat(response.jsonPath().getList(JSON_PATH_CHANGES), hasSize(2));
    assertThat(
        response.jsonPath().getString(JSON_PATH_CHANGES + "[0]." + JSON_PATH_CONFIG_TYPE),
        isA(String.class));
    assertThat(
        response.jsonPath().getString(JSON_PATH_CHANGES + "[0]." + JSON_PATH_OPERATION_TYPE),
        isA(String.class));
    assertThat(
        response.jsonPath().getString(JSON_PATH_CHANGES + "[0]." + JSON_PATH_CHANGED_BY),
        isA(String.class));
    assertThat(
        response.jsonPath().getString(JSON_PATH_CHANGES + "[0]." + JSON_PATH_CHANGED_AT),
        isA(String.class));
  }

  @Test
  @DisplayName("Should get changelog by id")
  public void testGetChangelogById() {
    String oldValues = "{\"key1\":\"value1\"}";
    String newValues = "{\"key1\":\"value2\"}";

    Long changelogId =
        insertChangelog(
            TENANT_1, CONFIG_TYPE_OTP_CONFIG, OPERATION_UPDATE, oldValues, newValues, ADMIN_EMAIL);

    Response response = getChangelogById(TENANT_1, changelogId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getLong("id"), equalTo(changelogId));
    assertThat(response.jsonPath().getString(JSON_PATH_TENANT_ID), equalTo(TENANT_1));
    assertThat(
        response.jsonPath().getString(JSON_PATH_CONFIG_TYPE), equalTo(CONFIG_TYPE_OTP_CONFIG));
    assertThat(response.jsonPath().getString(JSON_PATH_OPERATION_TYPE), equalTo(OPERATION_UPDATE));
    assertThat(response.jsonPath().getString(JSON_PATH_CHANGED_BY), equalTo(ADMIN_EMAIL));
    assertThat(response.jsonPath().getString(JSON_PATH_CHANGED_AT), notNullValue());
    assertThat(response.jsonPath().getMap(JSON_PATH_OLD_VALUES), notNullValue());
    assertThat(response.jsonPath().getMap(JSON_PATH_NEW_VALUES), notNullValue());
  }

  @Test
  @DisplayName("Should return 400 when changelog not found")
  public void testGetChangelogByIdNotFound() {
    Response response = getChangelogById(TENANT_1, 99999L);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("changelog_not_found"));
  }

  @Test
  @DisplayName("Should return error when tenant_id is missing")
  public void testGetChangelogMissingTenantId() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(QUERY_PARAM_LIMIT, DEFAULT_LIMIT);
    queryParams.put(QUERY_PARAM_OFFSET, DEFAULT_OFFSET);

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(QUERY_PARAM_TENANT_ID + " is required"));
  }

  @Test
  @DisplayName("Should return error when limit is invalid")
  public void testGetChangelogInvalidLimit() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(QUERY_PARAM_TENANT_ID, TENANT_1);
    queryParams.put(QUERY_PARAM_LIMIT, "101");
    queryParams.put(QUERY_PARAM_OFFSET, DEFAULT_OFFSET);

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("limit must be between 1 and 100"));
  }

  @Test
  @DisplayName("Should use default offset value of 0 when offset is not provided")
  public void testGetChangelogDefaultOffset() {
    String oldValues = "{\"key1\":\"value1\"}";
    String newValues = "{\"key1\":\"value2\"}";

    insertChangelog(
        TENANT_1, CONFIG_TYPE_OTP_CONFIG, OPERATION_UPDATE, oldValues, newValues, ADMIN_EMAIL);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(QUERY_PARAM_TENANT_ID, TENANT_1);
    queryParams.put(QUERY_PARAM_LIMIT, DEFAULT_LIMIT);

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getLong(JSON_PATH_TOTAL), equalTo(1L));
    assertThat(response.jsonPath().getList(JSON_PATH_CHANGES), hasSize(1));
  }

  @Test
  @DisplayName("Should return error when offset is negative")
  public void testGetChangelogNegativeOffset() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(QUERY_PARAM_TENANT_ID, TENANT_1);
    queryParams.put(QUERY_PARAM_LIMIT, DEFAULT_LIMIT);
    queryParams.put(QUERY_PARAM_OFFSET, "-1");

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("offset must be greater than or equal to 0"));
  }

  @Test
  @DisplayName("Should return empty list when no changelog exists")
  public void testGetChangelogEmptyList() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(QUERY_PARAM_TENANT_ID, TENANT_1);
    queryParams.put(QUERY_PARAM_LIMIT, DEFAULT_LIMIT);
    queryParams.put(QUERY_PARAM_OFFSET, DEFAULT_OFFSET);

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getLong(JSON_PATH_TOTAL), equalTo(0L));
    assertThat(response.jsonPath().getList(JSON_PATH_CHANGES), hasSize(0));
  }

  @Test
  @DisplayName("Should respect limit parameter")
  public void testGetChangelogWithLimit() {
    String oldValues = "{\"key1\":\"value1\"}";
    String newValues = "{\"key1\":\"value2\"}";

    insertChangelog(
        TENANT_1, CONFIG_TYPE_OTP_CONFIG, OPERATION_UPDATE, oldValues, newValues, ADMIN_EMAIL);
    insertChangelog(
        TENANT_1, CONFIG_TYPE_EMAIL_CONFIG, OPERATION_INSERT, null, newValues, ADMIN_EMAIL);
    insertChangelog(TENANT_1, CONFIG_TYPE_SMS_CONFIG, "DELETE", oldValues, null, ADMIN_EMAIL);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(QUERY_PARAM_TENANT_ID, TENANT_1);
    queryParams.put(QUERY_PARAM_LIMIT, "2");
    queryParams.put(QUERY_PARAM_OFFSET, DEFAULT_OFFSET);

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getLong(JSON_PATH_TOTAL), equalTo(3L));
    assertThat(response.jsonPath().getList(JSON_PATH_CHANGES), hasSize(2));
  }

  @Test
  @DisplayName("Should persist changelog entries after tenant deletion")
  public void testChangelogPersistsAfterTenantDeletion() {
    String testTenantId = "t" + RandomStringUtils.randomAlphanumeric(9);
    String oldValues = "{\"key1\":\"value1\"}";
    String newValues = "{\"key1\":\"value2\"}";

    insertChangelog(
        testTenantId, CONFIG_TYPE_OTP_CONFIG, OPERATION_UPDATE, oldValues, newValues, ADMIN_EMAIL);
    insertChangelog(
        testTenantId, CONFIG_TYPE_EMAIL_CONFIG, OPERATION_INSERT, null, newValues, ADMIN_EMAIL);

    Long countBefore = countChangelogByTenant(testTenantId);
    assertThat(countBefore, equalTo(2L));

    deleteTenant(testTenantId);

    Long countAfter = countChangelogByTenant(testTenantId);
    assertThat(countAfter, equalTo(2L));

    cleanupChangelog(testTenantId);
  }
}
