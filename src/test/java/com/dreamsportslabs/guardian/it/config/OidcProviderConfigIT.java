package com.dreamsportslabs.guardian.it.config;

import static com.dreamsportslabs.guardian.Constants.*;
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
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_PROVIDER_NAME), equalTo(testProviderName));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_ISSUER),
        equalTo("https://accounts.google.com"));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_JWKS_URL),
        equalTo("https://www.googleapis.com/oauth2/v3/certs"));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(true));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_USER_IDENTIFIER), equalTo("email"));

    JsonObject dbConfig = DbUtils.getOidcProviderConfig(testTenantId, testProviderName);
    assertThat(dbConfig, org.hamcrest.Matchers.notNullValue());
    assertThat(dbConfig.getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(dbConfig.getString(REQUEST_FIELD_PROVIDER_NAME), equalTo(testProviderName));
    assertThat(dbConfig.getString(REQUEST_FIELD_ISSUER), equalTo("https://accounts.google.com"));
  }

  @Test
  @DisplayName("Should create oidc_provider_config with default values when not provided")
  public void testCreateOidcProviderConfigWithDefaults() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    requestBody.put(REQUEST_FIELD_PROVIDER_NAME, testProviderName);
    requestBody.put(REQUEST_FIELD_ISSUER, "https://accounts.google.com");
    requestBody.put(REQUEST_FIELD_JWKS_URL, "https://www.googleapis.com/oauth2/v3/certs");
    requestBody.put(REQUEST_FIELD_TOKEN_URL, "https://oauth2.googleapis.com/token");
    requestBody.put(REQUEST_FIELD_CLIENT_ID, "test-client-id");
    requestBody.put(REQUEST_FIELD_CLIENT_SECRET, "test-client-secret");
    requestBody.put(REQUEST_FIELD_REDIRECT_URI, "https://example.com/callback");
    requestBody.put(REQUEST_FIELD_CLIENT_AUTH_METHOD, "client_secret_post");
    Map<String, Object> audienceClaims = new HashMap<>();
    audienceClaims.put("aud1", "value1");
    audienceClaims.put("aud2", "value2");
    requestBody.put(REQUEST_FIELD_AUDIENCE_CLAIMS, audienceClaims);

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_CREATED);
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(true));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_USER_IDENTIFIER), equalTo("email"));
  }

  @Test
  @DisplayName("Should return error when provider_name is blank")
  public void testCreateOidcProviderConfigBlankProviderName() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_PROVIDER_NAME, "");

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
    requestBody.put(REQUEST_FIELD_PROVIDER_NAME, RandomStringUtils.randomAlphanumeric(51));

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_PROVIDER_NAME_CANNOT_EXCEED_50));
  }

  @Test
  @DisplayName("Should return error when issuer is blank")
  public void testCreateOidcProviderConfigBlankIssuer() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_ISSUER, "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("issuer must be a valid HTTP/HTTPS URL"));
  }

  @Test
  @DisplayName("Should return error when jwks_url is blank")
  public void testCreateOidcProviderConfigBlankJwksUrl() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_JWKS_URL, "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("jwks_url must be a valid HTTP/HTTPS URL"));
  }

  @Test
  @DisplayName("Should return error when token_url is blank")
  public void testCreateOidcProviderConfigBlankTokenUrl() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_TOKEN_URL, "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("token_url must be a valid HTTP/HTTPS URL"));
  }

  @Test
  @DisplayName("Should return error when client_id is blank")
  public void testCreateOidcProviderConfigBlankClientId() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_CLIENT_ID, "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_ID_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when client_id exceeds 256 characters")
  public void testCreateOidcProviderConfigClientIdTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_CLIENT_ID, RandomStringUtils.randomAlphanumeric(257));

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_ID_CANNOT_EXCEED_256));
  }

  @Test
  @DisplayName("Should return error when client_secret is blank")
  public void testCreateOidcProviderConfigBlankClientSecret() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_CLIENT_SECRET, "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_SECRET_CANNOT_BE_BLANK));
  }

  @Test
  @DisplayName("Should return error when redirect_uri is blank")
  public void testCreateOidcProviderConfigBlankRedirectUri() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_REDIRECT_URI, "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo("redirect_uri must be a valid HTTP/HTTPS URL"));
  }

  @Test
  @DisplayName("Should return error when client_auth_method is blank")
  public void testCreateOidcProviderConfigBlankClientAuthMethod() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_CLIENT_AUTH_METHOD, "");

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_AUTH_METHOD_INVALID));
  }

  @Test
  @DisplayName("Should return error when client_auth_method is invalid")
  public void testCreateOidcProviderConfigClientAuthMethodTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_CLIENT_AUTH_METHOD, RandomStringUtils.randomAlphanumeric(257));

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_CLIENT_AUTH_METHOD_INVALID));
  }

  @Test
  @DisplayName("Should return error when user_identifier exceeds 20 characters")
  public void testCreateOidcProviderConfigUserIdentifierTooLong() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_USER_IDENTIFIER, RandomStringUtils.randomAlphanumeric(21));

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_USER_IDENTIFIER_CANNOT_EXCEED_20));
  }

  @Test
  @DisplayName("Should return error when audience_claims is null")
  public void testCreateOidcProviderConfigNullAudienceClaims() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> requestBody = createOidcProviderConfigBody();
    requestBody.put(REQUEST_FIELD_AUDIENCE_CLAIMS, null);

    Response response = createOidcProviderConfig(testTenantId, requestBody);

    response.then().statusCode(SC_BAD_REQUEST).rootPath(ERROR).body(CODE, equalTo(INVALID_REQUEST));
    assertThat(
        response.jsonPath().getString(ERROR + "." + MESSAGE),
        equalTo(ERROR_MSG_AUDIENCE_CLAIMS_CANNOT_BE_NULL));
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
        equalTo(ERROR_CODE_OIDC_PROVIDER_CONFIG_ALREADY_EXISTS));
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
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_PROVIDER_NAME), equalTo(testProviderName));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_ISSUER),
        equalTo("https://accounts.google.com"));
  }

  @Test
  @DisplayName("Should return 404 when oidc_provider_config not found")
  public void testGetOidcProviderConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Response response = getOidcProviderConfig(testTenantId, testProviderName);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_OIDC_PROVIDER_CONFIG_NOT_FOUND));
  }

  @Test
  @DisplayName("Should update oidc_provider_config successfully with single field")
  public void testUpdateOidcProviderConfigSingleField() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcProviderConfig(testTenantId, createOidcProviderConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_ISSUER, "https://accounts.google.com/v2");

    Response response = updateOidcProviderConfig(testTenantId, testProviderName, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_ISSUER),
        equalTo("https://accounts.google.com/v2"));
    assertThat(response.jsonPath().getString(RESPONSE_FIELD_TENANT_ID), equalTo(testTenantId));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_PROVIDER_NAME), equalTo(testProviderName));

    JsonObject dbConfig = DbUtils.getOidcProviderConfig(testTenantId, testProviderName);
    assertThat(dbConfig.getString(REQUEST_FIELD_ISSUER), equalTo("https://accounts.google.com/v2"));
  }

  @Test
  @DisplayName("Should update oidc_provider_config successfully with multiple fields")
  public void testUpdateOidcProviderConfigMultipleFields() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcProviderConfig(testTenantId, createOidcProviderConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_ISSUER, "https://accounts.google.com/v2");
    updateBody.put(REQUEST_FIELD_IS_SSL_ENABLED, false);
    updateBody.put(REQUEST_FIELD_USER_IDENTIFIER, "username");

    Response response = updateOidcProviderConfig(testTenantId, testProviderName, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_ISSUER),
        equalTo("https://accounts.google.com/v2"));
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(false));
    assertThat(response.jsonPath().getString(REQUEST_FIELD_USER_IDENTIFIER), equalTo("username"));

    JsonObject dbConfig = DbUtils.getOidcProviderConfig(testTenantId, testProviderName);
    assertThat(dbConfig.getString(REQUEST_FIELD_ISSUER), equalTo("https://accounts.google.com/v2"));
    assertThat(dbConfig.getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(false));
  }

  @Test
  @DisplayName("Should update oidc_provider_config partially - only provided fields")
  public void testUpdateOidcProviderConfigPartial() {
    createTenant(createTenantBody()).then().statusCode(201);
    createOidcProviderConfig(testTenantId, createOidcProviderConfigBody())
        .then()
        .statusCode(SC_CREATED);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_IS_SSL_ENABLED, false);

    Response response = updateOidcProviderConfig(testTenantId, testProviderName, updateBody);

    response.then().statusCode(SC_OK);
    assertThat(response.jsonPath().getBoolean(REQUEST_FIELD_IS_SSL_ENABLED), equalTo(false));
    assertThat(
        response.jsonPath().getString(REQUEST_FIELD_ISSUER),
        equalTo("https://accounts.google.com"));
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
        .body(CODE, equalTo(NO_FIELDS_TO_UPDATE));
  }

  @Test
  @DisplayName("Should return 404 when oidc_provider_config not found for update")
  public void testUpdateOidcProviderConfigNotFound() {
    createTenant(createTenantBody()).then().statusCode(201);

    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put(REQUEST_FIELD_ISSUER, "https://accounts.google.com/v2");

    Response response = updateOidcProviderConfig(testTenantId, testProviderName, updateBody);

    response.then().statusCode(SC_NOT_FOUND).rootPath(ERROR);
    assertThat(
        response.jsonPath().getString(ERROR + "." + CODE),
        equalTo(ERROR_CODE_OIDC_PROVIDER_CONFIG_NOT_FOUND));
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
        equalTo(ERROR_CODE_OIDC_PROVIDER_CONFIG_NOT_FOUND));
  }

  private Map<String, Object> createTenantBody() {
    Map<String, Object> tenantBody = new HashMap<>();
    tenantBody.put(REQUEST_FIELD_ID, testTenantId);
    tenantBody.put(REQUEST_FIELD_NAME, testTenantName);
    return tenantBody;
  }

  private Map<String, Object> createOidcProviderConfigBody() {
    Map<String, Object> oidcProviderConfigBody = new HashMap<>();
    oidcProviderConfigBody.put(REQUEST_FIELD_TENANT_ID, testTenantId);
    oidcProviderConfigBody.put(REQUEST_FIELD_PROVIDER_NAME, testProviderName);
    oidcProviderConfigBody.put(REQUEST_FIELD_ISSUER, "https://accounts.google.com");
    oidcProviderConfigBody.put(
        REQUEST_FIELD_JWKS_URL, "https://www.googleapis.com/oauth2/v3/certs");
    oidcProviderConfigBody.put(REQUEST_FIELD_TOKEN_URL, "https://oauth2.googleapis.com/token");
    oidcProviderConfigBody.put(REQUEST_FIELD_CLIENT_ID, "test-client-id");
    oidcProviderConfigBody.put(REQUEST_FIELD_CLIENT_SECRET, "test-client-secret");
    oidcProviderConfigBody.put(REQUEST_FIELD_REDIRECT_URI, "https://example.com/callback");
    oidcProviderConfigBody.put(REQUEST_FIELD_CLIENT_AUTH_METHOD, "client_secret_post");
    oidcProviderConfigBody.put(REQUEST_FIELD_IS_SSL_ENABLED, true);
    oidcProviderConfigBody.put(REQUEST_FIELD_USER_IDENTIFIER, "email");
    Map<String, Object> audienceClaims = new HashMap<>();
    audienceClaims.put("aud1", "value1");
    audienceClaims.put("aud2", "value2");
    oidcProviderConfigBody.put(REQUEST_FIELD_AUDIENCE_CLAIMS, audienceClaims);
    return oidcProviderConfigBody;
  }
}
