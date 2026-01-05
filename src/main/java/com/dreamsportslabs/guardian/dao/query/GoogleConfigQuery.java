package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class GoogleConfigQuery {

  public static final String CREATE_GOOGLE_CONFIG =
      """
            INSERT INTO google_config (
                tenant_id, client_id, client_secret
            )
            VALUES (?, ?, ?)
            """;

  public static final String GET_GOOGLE_CONFIG =
      """
            SELECT tenant_id, client_id, client_secret
            FROM google_config
            WHERE tenant_id = ?
            """;

  public static final String UPDATE_GOOGLE_CONFIG =
      """
            UPDATE google_config
            SET client_id = ?,
                client_secret = ?
            WHERE tenant_id = ?
            """;

  public static final String DELETE_GOOGLE_CONFIG =
      """
            DELETE FROM google_config
            WHERE tenant_id = ?
            """;
}
