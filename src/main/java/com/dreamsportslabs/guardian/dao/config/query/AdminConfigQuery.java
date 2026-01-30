package com.dreamsportslabs.guardian.dao.config.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class AdminConfigQuery {

  public static final String CREATE_ADMIN_CONFIG =
      """
      INSERT INTO admin_config (username, password, tenant_id)
      VALUES (?, ?, ?)
      """;

  public static final String GET_ADMIN_CONFIG =
      """
      SELECT tenant_id, username, password
      FROM admin_config
      WHERE tenant_id = ?
      """;

  public static final String UPDATE_ADMIN_CONFIG =
      """
      UPDATE admin_config
      SET username = ?,
          password = ?
      WHERE tenant_id = ?
      """;

  public static final String DELETE_ADMIN_CONFIG =
      """
      DELETE FROM admin_config
      WHERE tenant_id = ?
      """;
}
