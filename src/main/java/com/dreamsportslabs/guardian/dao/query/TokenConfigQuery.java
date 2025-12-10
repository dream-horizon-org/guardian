package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class TokenConfigQuery {

  public static final String GET_TOKEN_CONFIG =
      """
            SELECT tenant_id, algorithm, issuer, rsa_keys,
                   access_token_expiry, refresh_token_expiry, id_token_expiry, id_token_claims,
                   cookie_same_site, cookie_domain, cookie_path, cookie_secure, cookie_http_only,
                   access_token_claims
            FROM token_config
            WHERE tenant_id = ?
            """;

  public static final String UPDATE_TOKEN_CONFIG =
      """
            UPDATE token_config
            SET algorithm = ?,
                issuer = ?,
                rsa_keys = ?,
                access_token_expiry = ?,
                refresh_token_expiry = ?,
                id_token_expiry = ?,
                id_token_claims = ?,
                cookie_same_site = ?,
                cookie_domain = ?,
                cookie_path = ?,
                cookie_secure = ?,
                cookie_http_only = ?,
                access_token_claims = ?
            WHERE tenant_id = ?
            """;
}
