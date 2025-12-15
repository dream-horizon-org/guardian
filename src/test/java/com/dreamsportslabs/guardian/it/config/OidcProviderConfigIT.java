package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.CODE;
import static com.dreamsportslabs.guardian.Constants.ERROR;
import static com.dreamsportslabs.guardian.Constants.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.Constants.MESSAGE;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createOidcProviderConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.createTenant;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.deleteOidcProviderConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getOidcProviderConfig;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.updateOidcProviderConfig;
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

public class OidcProviderConfigIT {

  private String testTenantId;
  private String testTenantName;
  private String testProviderName;

  @BeforeEach
  void setUp() {
    testTenantId = "test" + RandomStringUtils.randomAlphanumeric(6);
    testTenantName = "Test Tenant " + RandomStringUtils.randomAlphanumeric(4);
    testProviderName = "google";
    cleanupChangelog(testTenantId);
    DbUtils.deleteTenant(testTenantId);
  }

  @AfterEach
  void tearDown() {
    cleanupChangelog(testTenantId);
    DbUtils.deleteTenant(testTenantId);
  }

  @Test
  @DisplayName("Should create oidc_provider_config successfully")
  public void testCreateOidcProviderConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = createOidcProviderConfig(testTenantId, createOidcProviderConfigBody());

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("provider_name"), equalTo(testProviderName));
    assertThat(response.jsonPath().getString("issuer"), equalTo("https://accounts.google.com"));
    assertThat(
        response.jsonPath().getString("jwks_url"),
        equalTo("https://www.googleapis.com/oauth2/v3/certs"));
    assertThat(response.jsonPath().getBoolean("is_ssl_enabled"), equalTo(true));
    assertThat(response.jsonPath().getString("user_identifier"), equalTo("email"));

    JsonObject dbConfig = DbUtils.getOidcProviderConfig(testTenantId, testProviderName);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString("tenant_id"), equalTo(testTenantId));
    assertThat(dbConfig.getString("provider_name"), equalTo(testProviderName));
    assertThat(dbConfig.getString("issuer"), equalTo("https://accounts.google.com"));
  }

  @Test
  @DisplayName("Should create oidc_provider_config with default values when not provided")
  public void testCreateOidcProviderConfigWithDefaults() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tenant_id", testTenantId);
    requestBody.put("provider_name", testProviderName);
    requestBody.put("issuer", "https://accounts.google.com");
    requestBody.put("jwks_url", "https://www.googleapis.com/oauth2/v3/certs");
    requestBody.put("token_url", "https://oauth2.googleapis.com/token");
    requestBody.put("client_id", "test-client-id");
    requestBody.put("client_secret", "test-client-secret");
    requestBody.put("redirect_uri", "https://example.com/callback");
    requestBody.put("client_auth_method", "client_secret_post");
    List<String> audienceClaims = new ArrayList<>();
    audienceClaims.add("aud1");
    audienceClaims.add("aud2");
    requestBody.put("audience_claims", audienceClaims);

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getBoolean("is_ssl_enabled"), equalTo(false));
    assertThat(response.jsonPath().getString("user_identifier"), equalTo("email"));
  }

  @Test
  @DisplayName("Should return error when tenant_id is blank")
  public void testCreateOidcProviderConfigBlankTenantId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("tenant_id", "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("tenant_id cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when tenant_id exceeds 10 characters")
  public void testCreateOidcProviderConfigTenantIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("tenant_id", RandomStringUtils.randomAlphanumeric(11));

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant_id cannot exceed 10 characters"));
  }

  @Test
  @DisplayName("Should return error when provider_name is blank")
  public void testCreateOidcProviderConfigBlankProviderName() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("provider_name", "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("provider_name cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when provider_name exceeds 50 characters")
  public void testCreateOidcProviderConfigProviderNameTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("provider_name", RandomStringUtils.randomAlphanumeric(51));

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("provider_name cannot exceed 50 characters"));
  }

  @Test
  @DisplayName("Should return error when issuer is blank")
  public void testCreateOidcProviderConfigBlankIssuer() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("issuer", "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("issuer cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when jwks_url is blank")
  public void testCreateOidcProviderConfigBlankJwksUrl() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("jwks_url", "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("jwks_url cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when token_url is blank")
  public void testCreateOidcProviderConfigBlankTokenUrl() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("token_url", "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("token_url cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when client_id is blank")
  public void testCreateOidcProviderConfigBlankClientId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("client_id", "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE), equalTo("client_id cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when client_id exceeds 256 characters")
  public void testCreateOidcProviderConfigClientIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("client_id", RandomStringUtils.randomAlphanumeric(257));

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("client_id cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when client_secret is blank")
  public void testCreateOidcProviderConfigBlankClientSecret() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("client_secret", "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("client_secret cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when redirect_uri is blank")
  public void testCreateOidcProviderConfigBlankRedirectUri() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("redirect_uri", "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("redirect_uri cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when client_auth_method is blank")
  public void testCreateOidcProviderConfigBlankClientAuthMethod() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("client_auth_method", "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("client_auth_method cannot be blank"));
  }

  @Test
  @DisplayName("Should return error when client_auth_method exceeds 256 characters")
  public void testCreateOidcProviderConfigClientAuthMethodTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("client_auth_method", RandomStringUtils.randomAlphanumeric(257));

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("client_auth_method cannot exceed 256 characters"));
  }

  @Test
  @DisplayName("Should return error when user_identifier exceeds 20 characters")
  public void testCreateOidcProviderConfigUserIdentifierTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("user_identifier", RandomStringUtils.randomAlphanumeric(21));

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("user_identifier cannot exceed 20 characters"));
  }

  @Test
  @DisplayName("Should return error when audience_claims is null")
  public void testCreateOidcProviderConfigNullAudienceClaims() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put("audience_claims", null);

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("audience_claims cannot be null"));
  }

  @Test
  @DisplayName("Should return error when tenant-id header doesn't match body tenant_id")
  public void testCreateOidcProviderConfigHeaderMismatch() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    String differentTenantId = "diff" + RandomStringUtils.randomAlphanumeric(6);
    requestBody.put("tenant_id", differentTenantId);

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("tenant-id header must match tenant_id in request body"));
  }

  @Test
  @DisplayName("Should return error when oidc_provider_config already exists")
  public void testCreateOidcProviderConfigDuplicate() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcProviderConfig(testTenantId, createOidcProviderConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Response response = createOidcProviderConfig(testTenantId, createOidcProviderConfigBody());

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo("oidc_provider_config_already_exists"));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("OIDC provider config already exists: " + testTenantId + "/" + testProviderName));
  }

  @Test
  @DisplayName("Should get oidc_provider_config successfully")
  public void testGetOidcProviderConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcProviderConfig(testTenantId, createOidcProviderConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Response response = getOidcProviderConfig(testTenantId, testProviderName);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("provider_name"), equalTo(testProviderName));
    assertThat(response.jsonPath().getString("issuer"), equalTo("https://accounts.google.com"));
  }

  @Test
  @DisplayName("Should return 404 when oidc_provider_config not found")
  public void testGetOidcProviderConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getOidcProviderConfig(testTenantId, testProviderName);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo("oidc_provider_config_not_found"));
  }

  @Test
  @DisplayName("Should update oidc_provider_config successfully with single field")
  public void testUpdateOidcProviderConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcProviderConfig(testTenantId, createOidcProviderConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("issuer", "https://accounts.google.com/v2");

    Response response = updateOidcProviderConfig(testTenantId, testProviderName, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("issuer"), equalTo("https://accounts.google.com/v2"));
    assertThat(response.jsonPath().getString("tenant_id"), equalTo(testTenantId));
    assertThat(response.jsonPath().getString("provider_name"), equalTo(testProviderName));

    JsonObject dbConfig = DbUtils.getOidcProviderConfig(testTenantId, testProviderName);
    assertThat(dbConfig.getString("issuer"), equalTo("https://accounts.google.com/v2"));
  }

  @Test
  @DisplayName("Should update oidc_provider_config successfully with multiple fields")
  public void testUpdateOidcProviderConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcProviderConfig(testTenantId, createOidcProviderConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("issuer", "https://accounts.google.com/v2");
    updateBody.put("is_ssl_enabled", false);
    updateBody.put("user_identifier", "username");

    Response response = updateOidcProviderConfig(testTenantId, testProviderName, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getString("issuer"), equalTo("https://accounts.google.com/v2"));
    assertThat(response.jsonPath().getBoolean("is_ssl_enabled"), equalTo(false));
    assertThat(response.jsonPath().getString("user_identifier"), equalTo("username"));

    JsonObject dbConfig = DbUtils.getOidcProviderConfig(testTenantId, testProviderName);
    assertThat(dbConfig.getString("issuer"), equalTo("https://accounts.google.com/v2"));
    assertThat(dbConfig.getBoolean("is_ssl_enabled"), equalTo(false));
  }

  @Test
  @DisplayName("Should update oidc_provider_config partially - only provided fields")
  public void testUpdateOidcProviderConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcProviderConfig(testTenantId, createOidcProviderConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("is_ssl_enabled", false);

    Response response = updateOidcProviderConfig(testTenantId, testProviderName, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean("is_ssl_enabled"), equalTo(false));
    assertThat(response.jsonPath().getString("issuer"), equalTo("https://accounts.google.com"));
  }

  @Test
  @DisplayName("Should return error when no fields to update")
  public void testUpdateOidcProviderConfigNoFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcProviderConfig(testTenantId, createOidcProviderConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();

    Response response = updateOidcProviderConfig(testTenantId, testProviderName, updateBody);

    response
        .then()
        .statusCode(SC_BAD_REQUEST)
        .rootPath(ERROR)
        .body(CODE, equalTo("no_fields_to_update"));
  }

  @Test
  @DisplayName("Should return 404 when oidc_provider_config not found for update")
  public void testUpdateOidcProviderConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("issuer", "https://accounts.google.com/v2");

    Response response = updateOidcProviderConfig(testTenantId, testProviderName, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo("oidc_provider_config_not_found"));
  }

  @Test
  @DisplayName("Should delete oidc_provider_config successfully")
  public void testDeleteOidcProviderConfigSuccess() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcProviderConfig(testTenantId, createOidcProviderConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Response response = deleteOidcProviderConfig(testTenantId, testProviderName);

    response.then().statusCode(SC_NO_CONTENT);

    JsonObject dbConfig = DbUtils.getOidcProviderConfig(testTenantId, testProviderName);
    assertThat(dbConfig, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @DisplayName("Should return 404 when oidc_provider_config not found for delete")
  public void testDeleteOidcProviderConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = deleteOidcProviderConfig(testTenantId, testProviderName);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo("oidc_provider_config_not_found"));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put("id", testTenantId);
    tenantBody.put("name", testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createOidcProviderConfigBody() {
    Map<String, Object> oidcProviderConfigBody = new HashMap<>();
    oidcProviderConfigBody.put("tenant_id", testTenantId);
    oidcProviderConfigBody.put("provider_name", testProviderName);
    oidcProviderConfigBody.put("issuer", "https://accounts.google.com");
    oidcProviderConfigBody.put("jwks_url", "https://www.googleapis.com/oauth2/v3/certs");
    oidcProviderConfigBody.put("token_url", "https://oauth2.googleapis.com/token");
    oidcProviderConfigBody.put("client_id", "test-client-id");
    oidcProviderConfigBody.put("client_secret", "test-client-secret");
    oidcProviderConfigBody.put("redirect_uri", "https://example.com/callback");
    oidcProviderConfigBody.put("client_auth_method", "client_secret_post");
    oidcProviderConfigBody.put("is_ssl_enabled", true);
    oidcProviderConfigBody.put("user_identifier", "email");
    List<String> audienceClaims = new ArrayList<>();
    audienceClaims.add("aud1");
    audienceClaims.add("aud2");
    oidcProviderConfigBody.put("audience_claims", audienceClaims);
    return oidcProviderConfigBody;
  }
}
