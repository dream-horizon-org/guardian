package com.dreamsportslabs.guardian.it;

import static com.dreamsportslabs.guardian.Constants.ERROR_GOOGLE_AUTH_NOT_CONFIGURED;
import static com.dreamsportslabs.guardian.Constants.ERROR_OIDC_CONFIG_NOT_EXISTS;
import static com.dreamsportslabs.guardian.Constants.HEADER_TENANT_ID;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.authGoogleV2;
import static com.dreamsportslabs.guardian.utils.ApplicationIoUtils.getOidcDiscovery;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class OptionalConfigIT {

  public static final String TENANT_MINIMAL = "tnt_min";
  public static final String TENANT_PARTIAL = "tnt_part";
  public static final String TENANT_FULL = "tenant1";
  public static final String CLIENT_ID = "client1";
  public static final String FAKE_ID_TOKEN = "fake-id-token";
  public static final String FLOW = "SIGNINUP";
  public static final String RESPONSE_TYPE = "token";
  public static final String ERROR_CODE = "error.code";

  @Test
  @DisplayName("Should return OIDC config for tenant with OIDC configured")
  void shouldReturnOidcConfigForPartialTenant() {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, TENANT_PARTIAL);

    Response response = getOidcDiscovery(headers, new HashMap<>());

    response.then().statusCode(HttpStatus.SC_OK);

    String issuer = response.jsonPath().getString("issuer");
    assertThat("issuer should not be empty", issuer, not(emptyString()));
  }

  @Test
  @DisplayName("Should return error for tenant without OIDC configured")
  void shouldReturnErrorForMinimalTenantWithoutOidc() {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, TENANT_MINIMAL);

    Response response = getOidcDiscovery(headers, new HashMap<>());

    response.then().statusCode(HttpStatus.SC_BAD_REQUEST);

    String errorCode = response.jsonPath().getString(ERROR_CODE);
    assertThat(errorCode, equalTo(ERROR_OIDC_CONFIG_NOT_EXISTS));
  }

  @Test
  @DisplayName("Should return error when Google auth is not configured for tenant")
  void shouldReturnErrorWhenGoogleNotConfigured() {
    Response response = authGoogleV2(TENANT_PARTIAL, CLIENT_ID, FAKE_ID_TOKEN, FLOW, RESPONSE_TYPE);

    response.then().statusCode(HttpStatus.SC_BAD_REQUEST);

    String errorCode = response.jsonPath().getString(ERROR_CODE);
    assertThat(errorCode, equalTo(ERROR_GOOGLE_AUTH_NOT_CONFIGURED));
  }

  @Test
  @DisplayName("Should return error when Google auth is not configured for minimal tenant")
  void shouldReturnErrorWhenGoogleNotConfiguredForMinimalTenant() {
    Response response = authGoogleV2(TENANT_MINIMAL, CLIENT_ID, FAKE_ID_TOKEN, FLOW, RESPONSE_TYPE);

    response.then().statusCode(HttpStatus.SC_BAD_REQUEST);

    String errorCode = response.jsonPath().getString(ERROR_CODE);
    assertThat(errorCode, equalTo(ERROR_GOOGLE_AUTH_NOT_CONFIGURED));
  }

  @Test
  @DisplayName("Minimal tenant should load successfully with only mandatory configs")
  void minimalTenantShouldLoadSuccessfully() {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, TENANT_MINIMAL);

    Response response =
        io.restassured.RestAssured.given()
            .headers(headers)
            .get("/v1/certs")
            .then()
            .extract()
            .response();

    response.then().statusCode(HttpStatus.SC_OK);
    assertThat(response.jsonPath().getList("keys"), notNullValue());
  }

  @Test
  @DisplayName("Partial tenant should load successfully with some optional configs")
  void partialTenantShouldLoadSuccessfully() {
    Map<String, String> headers = new HashMap<>();
    headers.put(HEADER_TENANT_ID, TENANT_PARTIAL);

    Response response = getOidcDiscovery(headers, new HashMap<>());
    response.then().statusCode(HttpStatus.SC_OK);

    Response jwksResponse =
        io.restassured.RestAssured.given()
            .headers(headers)
            .get("/v1/certs")
            .then()
            .extract()
            .response();

    jwksResponse.then().statusCode(HttpStatus.SC_OK);
  }
}
