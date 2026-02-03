package com.dreamsportslabs.guardian.dao.config.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class GuestConfigQuery {

  public static final String CREATE_GUEST_CONFIG =
      """
      INSERT INTO guest_config (is_encrypted, secret_key, allowed_scopes, tenant_id)
      VALUES (?, ?, ?, ?)
      """;

  public static final String GET_GUEST_CONFIG =
      """
      SELECT tenant_id, is_encrypted, secret_key, allowed_scopes
      FROM guest_config
      WHERE tenant_id = ?
      """;

  public static final String UPDATE_GUEST_CONFIG =
      """
      UPDATE guest_config
      SET is_encrypted = ?,
          secret_key = ?,
          allowed_scopes = ?
      WHERE tenant_id = ?
      """;

  public static final String DELETE_GUEST_CONFIG =
      """
      DELETE FROM guest_config
      WHERE tenant_id = ?
      """;
}
