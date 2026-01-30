package com.dreamsportslabs.guardian.dao.config.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ContactVerifyConfigQuery {

  public static final String CREATE_CONTACT_VERIFY_CONFIG =
      """
      INSERT INTO contact_verify_config (
          is_otp_mocked, otp_length, try_limit,
          resend_limit, otp_resend_interval, otp_validity, whitelisted_inputs, tenant_id
      )
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      """;

  public static final String GET_CONTACT_VERIFY_CONFIG =
      """
      SELECT tenant_id, is_otp_mocked, otp_length, try_limit,
             resend_limit, otp_resend_interval, otp_validity, whitelisted_inputs
      FROM contact_verify_config
      WHERE tenant_id = ?
      """;

  public static final String UPDATE_CONTACT_VERIFY_CONFIG =
      """
      UPDATE contact_verify_config
      SET is_otp_mocked = ?,
          otp_length = ?,
          try_limit = ?,
          resend_limit = ?,
          otp_resend_interval = ?,
          otp_validity = ?,
          whitelisted_inputs = ?
      WHERE tenant_id = ?
      """;

  public static final String DELETE_CONTACT_VERIFY_CONFIG =
      """
      DELETE FROM contact_verify_config
      WHERE tenant_id = ?
      """;
}
