package com.dreamsportslabs.guardian.dao.config.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class AuthCodeConfigQuery {

  public static final String CREATE_AUTH_CODE_CONFIG =
      """
      INSERT INTO auth_code_config (ttl, length, tenant_id)
      VALUES (?, ?, ?)
      """;

  public static final String GET_AUTH_CODE_CONFIG =
      """
      SELECT tenant_id, ttl, length
      FROM auth_code_config
      WHERE tenant_id = ?
      """;

  public static final String UPDATE_AUTH_CODE_CONFIG =
      """
      UPDATE auth_code_config
      SET ttl = ?,
          length = ?
      WHERE tenant_id = ?
      """;

  public static final String DELETE_AUTH_CODE_CONFIG =
      """
      DELETE FROM auth_code_config
      WHERE tenant_id = ?
      """;
}
