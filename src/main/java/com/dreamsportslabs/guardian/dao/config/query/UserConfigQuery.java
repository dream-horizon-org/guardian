package com.dreamsportslabs.guardian.dao.config.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class UserConfigQuery {

  public static final String CREATE_USER_CONFIG =
      """
      INSERT INTO user_config (
          is_ssl_enabled, host, port,
          get_user_path, create_user_path, authenticate_user_path,
          add_provider_path, update_user_path, send_provider_details, tenant_id
      )
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

  public static final String GET_USER_CONFIG =
      """
      SELECT tenant_id, is_ssl_enabled, host, port,
             get_user_path, create_user_path, authenticate_user_path,
             add_provider_path, update_user_path, send_provider_details
      FROM user_config
      WHERE tenant_id = ?
      """;

  public static final String UPDATE_USER_CONFIG =
      """
      UPDATE user_config
      SET is_ssl_enabled = ?,
          host = ?,
          port = ?,
          get_user_path = ?,
          create_user_path = ?,
          authenticate_user_path = ?,
          add_provider_path = ?,
          update_user_path = ?,
          send_provider_details = ?
      WHERE tenant_id = ?
      """;
}
