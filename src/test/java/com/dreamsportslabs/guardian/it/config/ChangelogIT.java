package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.TENANT_1;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getChangelog;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getChangelogById;
import static com.dreamsportslabs.guardian.utils.DbUtils.cleanupChangelog;
import static com.dreamsportslabs.guardian.utils.DbUtils.insertChangelog;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
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

    insertChangelog(TENANT_1, "otp_config", "UPDATE", oldValues, newValues, "admin@example.com");
    insertChangelog(TENANT_1, "email_config", "INSERT", null, newValues, "admin@example.com");

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("tenant_id", TENANT_1);
    queryParams.put("limit", "50");

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("total"), equalTo(2));
    assertThat(response.jsonPath().getList("changes"), hasSize(2));
    assertThat(response.jsonPath().getString("changes[0].config_type"), isA(String.class));
    assertThat(response.jsonPath().getString("changes[0].operation_type"), isA(String.class));
    assertThat(response.jsonPath().getString("changes[0].changed_by"), isA(String.class));
    assertThat(response.jsonPath().getString("changes[0].changed_at"), isA(String.class));
  }

  @Test
  @DisplayName("Should get changelog by id")
  public void testGetChangelogById() {
    String oldValues = "{\"key1\":\"value1\"}";
    String newValues = "{\"key1\":\"value2\"}";

    Long changelogId =
        insertChangelog(
            TENANT_1, "otp_config", "UPDATE", oldValues, newValues, "admin@example.com");

    Response response = getChangelogById(TENANT_1, changelogId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getLong("id"), equalTo(changelogId));
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(TENANT_1));
    assertThat(response.jsonPath().getString("config_type"), equalTo("otp_config"));
    assertThat(response.jsonPath().getString("operation_type"), equalTo("UPDATE"));
    assertThat(response.jsonPath().getString("changed_by"), equalTo("admin@example.com"));
    assertThat(response.jsonPath().getString("changed_at"), notNullValue());
    assertThat(response.jsonPath().getMap("old_values"), notNullValue());
    assertThat(response.jsonPath().getMap("new_values"), notNullValue());
  }

  @Test
  @DisplayName("Should return 404 when changelog not found")
  public void testGetChangelogByIdNotFound() {
    Response response = getChangelogById(TENANT_1, 99999L);

    response
        .then()
        .statusCode(SC_NOT_FOUND)
        .rootPath(ERROR)
        .body(CODE, equalTo("changelog_not_found"));
  }

  @Test
  @DisplayName("Should return error when tenant_id is missing")
  public void testGetChangelogMissingTenantId() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("limit", "50");

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("tenant_id is required"));
  }

  @Test
  @DisplayName("Should return error when limit is invalid")
  public void testGetChangelogInvalidLimit() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("tenant_id", TENANT_1);
    queryParams.put("limit", "101");

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("limit must be between 1 and 100"));
  }

  @Test
  @DisplayName("Should return empty list when no changelog exists")
  public void testGetChangelogEmptyList() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("tenant_id", TENANT_1);
    queryParams.put("limit", "50");

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("total"), equalTo(0));
    assertThat(response.jsonPath().getList("changes"), hasSize(0));
  }

  @Test
  @DisplayName("Should respect limit parameter")
  public void testGetChangelogWithLimit() {
    String oldValues = "{\"key1\":\"value1\"}";
    String newValues = "{\"key1\":\"value2\"}";

    insertChangelog(TENANT_1, "otp_config", "UPDATE", oldValues, newValues, "admin@example.com");
    insertChangelog(TENANT_1, "email_config", "INSERT", null, newValues, "admin@example.com");
    insertChangelog(TENANT_1, "sms_config", "DELETE", oldValues, null, "admin@example.com");

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("tenant_id", TENANT_1);
    queryParams.put("limit", "2");

    Response response = getChangelog(TENANT_1, queryParams);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("total"), equalTo(3));
    assertThat(response.jsonPath().getList("changes"), hasSize(2));
  }
}
