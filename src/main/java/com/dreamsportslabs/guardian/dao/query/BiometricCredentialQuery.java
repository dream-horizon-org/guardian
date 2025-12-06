package com.dreamsportslabs.guardian.dao.query;

public class BiometricCredentialQuery {
  public static final String GET_CREDENTIAL_BY_ID =
      """
      SELECT id, tenant_id, client_id, user_id, credential_id, public_key, binding_type, alg,
             sign_count, aaguid, is_active, first_use_complete
      FROM credentials
      WHERE tenant_id = ? AND client_id = ? AND user_id = ? AND credential_id = ? AND is_active = 1
      """;

  public static final String UPDATE_SIGN_COUNT =
      """
      UPDATE credentials
      SET sign_count = ?, first_use_complete = TRUE
      WHERE id = ?
      """;

  public static final String UPSERT_CREDENTIAL =
      """
      INSERT INTO credentials (tenant_id, client_id, user_id, credential_id, public_key, binding_type, alg, sign_count, aaguid, revoked_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)
      ON DUPLICATE KEY UPDATE
        public_key = ?,
        binding_type = ?,
        alg = ?,
        sign_count = ?,
        aaguid = ?,
        revoked_at = NULL,
        updated_at = CURRENT_TIMESTAMP
      """;
}
