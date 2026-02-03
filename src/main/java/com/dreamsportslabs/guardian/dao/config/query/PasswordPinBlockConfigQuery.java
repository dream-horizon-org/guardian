package com.dreamsportslabs.guardian.dao.config.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class PasswordPinBlockConfigQuery {

  public static final String CREATE_PASSWORD_PIN_BLOCK_CONFIG =
      """
      INSERT INTO password_pin_block_config (
          attempts_allowed,
          attempts_window_seconds,
          block_interval_seconds,
          tenant_id
      )
      VALUES (?, ?, ?, ?)
      """;

  public static final String GET_PASSWORD_PIN_BLOCK_CONFIG =
      """
      SELECT attempts_allowed,
             attempts_window_seconds,
             block_interval_seconds
      FROM password_pin_block_config
      WHERE tenant_id = ?
      """;

  public static final String UPDATE_PASSWORD_PIN_BLOCK_CONFIG =
      """
      UPDATE password_pin_block_config
      SET attempts_allowed = ?,
          attempts_window_seconds = ?,
          block_interval_seconds = ?
      WHERE tenant_id = ?
      """;

  public static final String DELETE_PASSWORD_PIN_BLOCK_CONFIG =
      """
      DELETE FROM password_pin_block_config
      WHERE tenant_id = ?
      """;
}
