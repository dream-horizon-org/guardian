package com.dreamsportslabs.guardian.dao.config.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class OidcConfigQuery {

  public static final String CREATE_OIDC_CONFIG =
      """
      INSERT INTO oidc_config (
          issuer, authorization_endpoint, token_endpoint,
          userinfo_endpoint, revocation_endpoint, jwks_uri,
          grant_types_supported, response_types_supported, subject_types_supported,
          id_token_signing_alg_values_supported, token_endpoint_auth_methods_supported,
          login_page_uri, consent_page_uri, authorize_ttl, tenant_id
      )
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

  public static final String GET_OIDC_CONFIG =
      """
      SELECT tenant_id, issuer, authorization_endpoint, token_endpoint,
             userinfo_endpoint, revocation_endpoint, jwks_uri,
             grant_types_supported, response_types_supported, subject_types_supported,
             id_token_signing_alg_values_supported, token_endpoint_auth_methods_supported,
             login_page_uri, consent_page_uri, authorize_ttl
      FROM oidc_config
      WHERE tenant_id = ?
      """;

  public static final String UPDATE_OIDC_CONFIG =
      """
      UPDATE oidc_config
      SET issuer = ?,
          authorization_endpoint = ?,
          token_endpoint = ?,
          userinfo_endpoint = ?,
          revocation_endpoint = ?,
          jwks_uri = ?,
          grant_types_supported = ?,
          response_types_supported = ?,
          subject_types_supported = ?,
          id_token_signing_alg_values_supported = ?,
          token_endpoint_auth_methods_supported = ?,
          login_page_uri = ?,
          consent_page_uri = ?,
          authorize_ttl = ?
      WHERE tenant_id = ?
      """;

  public static final String DELETE_OIDC_CONFIG =
      """
      DELETE FROM oidc_config
      WHERE tenant_id = ?
      """;
}
