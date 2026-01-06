package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class EmailConfigQuery {

  public static final String CREATE_EMAIL_CONFIG =
      """
            INSERT INTO email_config (
                tenant_id, is_ssl_enabled, host, port,
                send_email_path, template_name, template_params
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

  public static final String GET_EMAIL_CONFIG =
      """
            SELECT tenant_id, is_ssl_enabled, host, port,
                   send_email_path, template_name, template_params
            FROM email_config
            WHERE tenant_id = ?
            """;

  public static final String UPDATE_EMAIL_CONFIG =
      """
            UPDATE email_config
            SET is_ssl_enabled = ?,
                host = ?,
                port = ?,
                send_email_path = ?,
                template_name = ?,
                template_params = ?
            WHERE tenant_id = ?
            """;

  public static final String DELETE_EMAIL_CONFIG =
      """
            DELETE FROM email_config
            WHERE tenant_id = ?
            """;
}
