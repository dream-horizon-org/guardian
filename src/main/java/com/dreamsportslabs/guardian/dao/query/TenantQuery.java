package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class TenantQuery {

  public static final String CREATE_TENANT =
      """
            INSERT INTO tenant (id, name)
            VALUES (?, ?)
            """;

  public static final String GET_TENANT =
      """
            SELECT id, name
            FROM tenant
            WHERE id = ?
            """;

  public static final String GET_TENANT_BY_NAME =
      """
            SELECT id, name
            FROM tenant
            WHERE name = ?
            """;

  public static final String UPDATE_TENANT =
      """
            UPDATE tenant
            SET name = ?
            WHERE id = ?
            """;

  public static final String DELETE_TENANT =
      """
            DELETE FROM tenant
            WHERE id = ?
            """;

  public static final String CREATE_USER_CONFIG =
      """
            INSERT INTO user_config (
                tenant_id, is_ssl_enabled, host, port,
                get_user_path, create_user_path, authenticate_user_path,
                add_provider_path, send_provider_details
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

  public static final String CREATE_TOKEN_CONFIG =
      """
            INSERT INTO token_config (
                tenant_id, algorithm, issuer, rsa_keys,
                access_token_expiry, refresh_token_expiry, id_token_expiry, id_token_claims,
                cookie_same_site, cookie_domain, cookie_path, cookie_secure, cookie_http_only,
                access_token_claims
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
}
