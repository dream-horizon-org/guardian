package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class UserConfigQuery {

  public static final String CREATE_USER_CONFIG =
      """
            INSERT INTO user_config (tenant_id, is_ssl_enabled, host, port, get_user_path, create_user_path, authenticate_user_path, add_provider_path, send_provider_details)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

  public static final String GET_USER_CONFIG =
      """
            SELECT tenant_id, is_ssl_enabled, host, port, get_user_path, create_user_path, authenticate_user_path, add_provider_path, send_provider_details
            FROM user_config
            WHERE tenant_id = ?
            """;

  public static final String UPDATE_USER_CONFIG =
      """
            UPDATE user_config SET <<insert_attributes>>
            WHERE tenant_id = ?
            """;

  public static final String DELETE_USER_CONFIG =
      """
            DELETE FROM user_config
            WHERE tenant_id = ?
            """;
}

