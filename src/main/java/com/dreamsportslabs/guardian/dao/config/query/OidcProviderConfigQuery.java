package com.dreamsportslabs.guardian.dao.config.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class OidcProviderConfigQuery {

  public static final String CREATE_OIDC_PROVIDER_CONFIG =
      """
      INSERT INTO oidc_provider_config (
          issuer, jwks_url, token_url,
          client_id, client_secret, redirect_uri, client_auth_method,
          is_ssl_enabled, user_identifier, audience_claims, tenant_id, provider_name
      )
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

  public static final String GET_OIDC_PROVIDER_CONFIG =
      """
      SELECT tenant_id, provider_name, issuer, jwks_url, token_url,
             client_id, client_secret, redirect_uri, client_auth_method,
             is_ssl_enabled, user_identifier, audience_claims
      FROM oidc_provider_config
      WHERE tenant_id = ? AND provider_name = ?
      """;

  public static final String UPDATE_OIDC_PROVIDER_CONFIG =
      """
      UPDATE oidc_provider_config
      SET issuer = ?,
          jwks_url = ?,
          token_url = ?,
          client_id = ?,
          client_secret = ?,
          redirect_uri = ?,
          client_auth_method = ?,
          is_ssl_enabled = ?,
          user_identifier = ?,
          audience_claims = ?
      WHERE tenant_id = ? AND provider_name = ?
      """;

  public static final String DELETE_OIDC_PROVIDER_CONFIG =
      """
      DELETE FROM oidc_provider_config
      WHERE tenant_id = ? AND provider_name = ?
      """;
}
