package com.dreamsportslabs.guardian.dao.config.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class SmsConfigQuery {

  public static final String CREATE_SMS_CONFIG =
      """
      INSERT INTO sms_config (
          is_ssl_enabled, host, port,
          send_sms_path, template_name, template_params, tenant_id
      )
      VALUES (?, ?, ?, ?, ?, ?, ?)
      """;

  public static final String GET_SMS_CONFIG =
      """
      SELECT tenant_id, is_ssl_enabled, host, port,
             send_sms_path, template_name, template_params
      FROM sms_config
      WHERE tenant_id = ?
      """;

  public static final String UPDATE_SMS_CONFIG =
      """
      UPDATE sms_config
      SET is_ssl_enabled = ?,
          host = ?,
          port = ?,
          send_sms_path = ?,
          template_name = ?,
          template_params = ?
      WHERE tenant_id = ?
      """;

  public static final String DELETE_SMS_CONFIG =
      """
      DELETE FROM sms_config
      WHERE tenant_id = ?
      """;
}
