package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.ERROR_MSG_ISSUER_CANNOT_BE_BLANK;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.Constants.NO_FIELDS_TO_UPDATE;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ID;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_ISSUER;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_NAME;
import static com.dreamsportslabs.guardian.Constants.REQUEST_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.Constants.RESPONSE_FIELD_TENANT_ID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createOidcConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteOidcConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getOidcConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateOidcConfig;
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

public class OidcConfigIT {

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
  @DisplayName("Should create oidc_config successfully")
  public void testCreateOidcConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createOidcConfig(testTenantId, createOidcConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_ISSUER), equalTo("https://example.com"));
    assertThat(
        response.jsonPath().getString("authorization_endpoint"),
        equalTo("https://example.com/authorize"));
    assertThat(response.jsonPath().getList("grant_types_supported").size(), equalTo(2));

    JsonObject dbConfig = DbUtils.getOidcConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(dbConfig.getString(REQUEST_FIELD_ISSUER), equalTo("https://example.com"));
  }

  @Test
  @DisplayName("Should return error when tenant_id is blank")
  public void testCreateOidcConfigBlankTenantId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcConfigBody();
    requestBody.put(REQUEST_FIELD_TENANT_ID, "");

    Response response = createOidcConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("tenant_id cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when issuer is blank")
  public void testCreateOidcConfigBlankIssuer() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcConfigBody();
    requestBody.put(REQUEST_FIELD_ISSUER, "");

    Response response = createOidcConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_ISSUER_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should create oidc_config with null JSON arrays (defaults to empty arrays)")
  public void testCreateOidcConfigWithNullJsonArrays() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcConfigBody();
    requestBody.put("grant_types_supported", null);
    requestBody.put("response_types_supported", null);
    requestBody.put("subject_types_supported", null);
    requestBody.put("id_token_signing_alg_values_supported", null);
    requestBody.put("token_endpoint_auth_methods_supported", null);

    Response response = createOidcConfig(testTenantId, requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getList("grant_types_supported").size(), equalTo(0));
    assertThat(response.jsonPath().getList("response_types_supported").size(), equalTo(0));
    assertThat(response.jsonPath().getList("subject_types_supported").size(), equalTo(0));
    assertThat(
        response.jsonPath().getList("id_token_signing_alg_values_supported").size(), equalTo(0));
    assertThat(
        response.jsonPath().getList("token_endpoint_auth_methods_supported").size(), equalTo(0));
  }

  @Test
  @DisplayName("Should return error when tenant-id header doesn't match body tenant_id")
  public void testCreateOidcConfigHeaderMismatch() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcConfigBody();
    String differentTenantId = "diff" + RandomStringUtils.randomAlphanumeric(6);
    requestBody.put(REQUEST_FIELD_TENANT_ID, differentTenantId);

    Response response = createOidcConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant-id header must match tenant_id in request body"));
  }

  @Test
  @DisplayName("Should return error when oidc_config already exists")
  public void testCreateOidcConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcConfig(testTenantId, createOidcConfigBody()).then().statusCode(SC_CREATED);

    Response response = createOidcConfig(testTenantId, createOidcConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE), equalTo("oidc_config_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("OIDC config already exists: " + testTenantId));
  }

  @Test
  @DisplayName("Should get oidc_config successfully")
  public void testGetOidcConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcConfig(testTenantId, createOidcConfigBody()).then().statusCode(SC_CREATED);

    Response response = getOidcConfig(testTenantId);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_ISSUER), equalTo("https://example.com"));
  }

  @Test
  @DisplayName("Should return 404 when oidc_config not found")
  public void testGetOidcConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getOidcConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(response.jsonPath().getString(ERROR + "." + CODE), equalTo("oidc_config_not_found"));
  }

  @Test
  @DisplayName("Should update oidc_config successfully with single field")
  public void testUpdateOidcConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcConfig(testTenantId, createOidcConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_ISSUER, "https://new-example.com");

    Response response = updateOidcConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("issuer"), equalTo("https://new-example.com"));
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));

    JsonObject dbConfig = DbUtils.getOidcConfig(testTenantId);
    assertThat(dbConfig.getString(REQUEST_FIELD_ISSUER), equalTo("https://new-example.com"));
  }

  @Test
  @DisplayName("Should update oidc_config successfully with multiple fields")
  public void testUpdateOidcConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcConfig(testTenantId, createOidcConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_ISSUER, "https://new-example.com");
    updateBody.put("authorize_ttl", 3600);
    List<String> newGrantTypes = new ArrayList<>();
    newGrantTypes.add("authorization_code");
    newGrantTypes.add("refresh_token");
    newGrantTypes.add("client_credentials");
    updateBody.put("grant_types_supported", newGrantTypes);

    Response response = updateOidcConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("issuer"), equalTo("https://new-example.com"));
    assertThat(response.jsonPath().getInt("authorize_ttl"), equalTo(3600));
    assertThat(response.jsonPath().getList("grant_types_supported").size(), equalTo(3));
  }

  @Test
  @DisplayName("Should update oidc_config partially - only provided fields")
  public void testUpdateOidcConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcConfig(testTenantId, createOidcConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("authorize_ttl", 3600);

    Response response = updateOidcConfig(testTenantId, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getInt("authorize_ttl"), equalTo(3600));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_ISSUER), equalTo("https://example.com"));
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateOidcConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcConfig(testTenantId, createOidcConfigBody()).then().statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateOidcConfig(testTenantId, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
  }

  @Test
  @DisplayName("Should return 404 when oidc_config not found for update")
  public void testUpdateOidcConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_ISSUER, "https://new-example.com");

    Response response = updateOidcConfig(testTenantId, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(response.jsonPath().getString(ERROR + "." + CODE), equalTo("oidc_config_not_found"));
  }

  @Test
  @DisplayName("Should delete oidc_config successfully")
  public void testDeleteOidcConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcConfig(testTenantId, createOidcConfigBody()).then().statusCode(SC_CREATED);

    Response response = deleteOidcConfig(testTenantId);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getOidcConfig(testTenantId);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 404 when oidc_config not found for delete")
  public void testDeleteOidcConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteOidcConfig(testTenantId);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(response.jsonPath().getString(ERROR + "." + CODE), equalTo("oidc_config_not_found"));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put(REQUEST_FIELD_ID, testTenantId);
    tenantBody.put(REQUEST_FIELD_NAME, testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createOidcConfigBody() {
    Map<String, Object> oidcConfigBody = new HashMap<>();
    oidcConfigBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    oidcConfigBody.put(REQUEST_FIELD_ISSUER, "https://example.com");
    oidcConfigBody.put("authorization_endpoint", "https://example.com/authorize");
    oidcConfigBody.put("token_endpoint", "https://example.com/token");
    oidcConfigBody.put("userinfo_endpoint", "https://example.com/userinfo");
    oidcConfigBody.put("revocation_endpoint", "https://example.com/revoke");
    oidcConfigBody.put("jwks_uri", "https://example.com/jwks");
    List<String> grantTypes = new ArrayList<>();
    grantTypes.add("authorization_code");
    grantTypes.add("refresh_token");
    oidcConfigBody.put("grant_types_supported", grantTypes);
    List<String> responseTypes = new ArrayList<>();
    responseTypes.add("code");
    oidcConfigBody.put("response_types_supported", responseTypes);
    List<String> subjectTypes = new ArrayList<>();
    subjectTypes.add("public");
    oidcConfigBody.put("subject_types_supported", subjectTypes);
    List<String> idTokenAlgs = new ArrayList<>();
    idTokenAlgs.add("RS256");
    oidcConfigBody.put("id_token_signing_alg_values_supported", idTokenAlgs);
    List<String> authMethods = new ArrayList<>();
    authMethods.add("client_secret_basic");
    oidcConfigBody.put("token_endpoint_auth_methods_supported", authMethods);
    return oidcConfigBody;
  }
}
