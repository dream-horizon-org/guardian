package com.dreamsportslabs.guardian.dao.query;

public class CredentialsQuery {
  public static final String GET_CREDENTIAL_BY_DEVICE_ID =
      """
      SELECT id, tenant_id, client_id, user_id, device_id, platform, credential_id, public_key, binding_type, alg,
             sign_count, aaguid, is_active, first_use_complete
      FROM credentials
      WHERE tenant_id = ? AND client_id = ? AND user_id = ? AND device_id = ?
      ORDER BY id DESC
      """;

  public static final String REVOKE_ACTIVE_CREDENTIALS_FOR_USER_DEVICE =
      """
      UPDATE credentials
      SET revoked_at = CURRENT_TIMESTAMP
      WHERE tenant_id = ? AND client_id = ? AND user_id = ? AND device_id = ? AND is_active = 1
      """;

  public static final String INSERT_CREDENTIAL =
      """
      INSERT INTO credentials (tenant_id, client_id, user_id, device_id, platform, credential_id, public_key, binding_type, alg, sign_count, aaguid, revoked_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)
      """;
}
