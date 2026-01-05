package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class OtpConfigQuery {

  public static final String CREATE_OTP_CONFIG =
      """
            INSERT INTO otp_config (
                tenant_id, is_otp_mocked, otp_length, try_limit,
                resend_limit, otp_resend_interval, otp_validity, whitelisted_inputs
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

  public static final String GET_OTP_CONFIG =
      """
            SELECT tenant_id, is_otp_mocked, otp_length, try_limit,
                   resend_limit, otp_resend_interval, otp_validity, whitelisted_inputs
            FROM otp_config
            WHERE tenant_id = ?
            """;

  public static final String UPDATE_OTP_CONFIG =
      """
            UPDATE otp_config
            SET is_otp_mocked = ?,
                otp_length = ?,
                try_limit = ?,
                resend_limit = ?,
                otp_resend_interval = ?,
                otp_validity = ?,
                whitelisted_inputs = ?
            WHERE tenant_id = ?
            """;

  public static final String DELETE_OTP_CONFIG =
      """
            DELETE FROM otp_config
            WHERE tenant_id = ?
            """;
}
