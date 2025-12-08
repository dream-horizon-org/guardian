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
            UPDATE tenant SET <<insert_attributes>>
            WHERE id = ?
            """;

  public static final String DELETE_TENANT =
      """
            DELETE FROM tenant
            WHERE id = ?
            """;
}
