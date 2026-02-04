package com.dreamsportslabs.guardian.dao.query;

public class RefreshTokenSql {

  public static final String SAVE_REFRESH_TOKEN =
      """
      INSERT INTO refresh_tokens (
          tenant_id, client_id, user_id, refresh_token,
          refresh_token_exp, scope, device_name, ip, source, location, auth_method
      ) VALUES (?, ?, ?, ?, ?, ?, ?, INET6_ATON(?), ?, ?, ?)
      """;

  public static final String GET_REFRESH_TOKEN_WITH_CLIENT_ID =
      """
      SELECT tenant_id, client_id, user_id, is_active, refresh_token, refresh_token_exp, scope, auth_method
      FROM refresh_tokens
      WHERE tenant_id = ? AND client_id = ? AND refresh_token = ? AND is_active = true
      """;

  public static final String GET_REFRESH_TOKEN =
      """
          SELECT tenant_id, client_id, user_id, is_active, refresh_token, refresh_token_exp, scope, auth_method
          FROM refresh_tokens
          WHERE tenant_id = ? AND refresh_token = ? AND is_active = true
      """;

  public static final String GET_ALL_REFRESH_TOKENS_FOR_USER =
      """
         SELECT refresh_token AS refreshToken
         FROM refresh_tokens
         WHERE tenant_id = ? AND user_id = ? AND is_active = 1
      """;

  public static final String GET_ALL_REFRESH_TOKENS_FOR_USER_AND_CLIENT =
      """
         SELECT refresh_token AS refreshToken
         FROM refresh_tokens
         WHERE tenant_id = ? AND client_id = ? AND is_active = 1 AND user_id = ?
      """;

  public static final String INVALIDATE_REFRESH_TOKEN =
      """
        UPDATE refresh_tokens
        SET is_active = false
        WHERE tenant_id = ? AND client_id = ? AND refresh_token = ?;
      """;

  public static final String INVALIDATE_ALL_REFRESH_TOKENS_FOR_USER =
      """
         UPDATE refresh_tokens
         SET is_active = false
         WHERE tenant_id = ? AND user_id = ?
      """;

  public static final String INVALIDATE_REFRESH_TOKENS_OF_CLIENT_FOR_USER =
      """
         UPDATE refresh_tokens
         SET is_active = false
         WHERE tenant_id = ? AND client_id = ? AND user_id = ?
      """;

  public static final String UPDATE_REFRESH_TOKEN_AUTH_METHOD =
      """
         UPDATE refresh_tokens
         SET auth_method = ?, scope = ?
         WHERE tenant_id = ? AND client_id = ? AND refresh_token = ?
      """;

  public static final String GET_ACTIVE_REFRESH_TOKENS_COUNT_FOR_USER_WITH_CLIENT =
      """
      SELECT COUNT(*) AS count
      FROM refresh_tokens
      WHERE tenant_id = ?
          AND client_id = ?
          AND user_id = ?
          AND is_active = 1
          AND refresh_token_exp > UNIX_TIMESTAMP()
      """;

  public static final String GET_ACTIVE_REFRESH_TOKENS_FOR_USER_WITH_CLIENT =
      """
      SELECT
          refresh_token,
          device_name,
          location,
          INET6_NTOA(ip) AS ip,
          source,
          DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS created_at
      FROM refresh_tokens
      WHERE tenant_id = ?
          AND client_id = ?
          AND user_id = ?
          AND is_active = 1
          AND refresh_token_exp > UNIX_TIMESTAMP()
      ORDER BY created_at DESC
      LIMIT ? OFFSET ?
      """;
}
