package com.dreamsportslabs.guardian.dao.config.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class FbConfigQuery {

  public static final String CREATE_FB_CONFIG =
      """
      INSERT INTO fb_config (app_id, app_secret, send_app_secret, tenant_id)
      VALUES (?, ?, ?, ?)
      """;

  public static final String GET_FB_CONFIG =
      """
      SELECT tenant_id, app_id, app_secret, send_app_secret
      FROM fb_config
      WHERE tenant_id = ?
      """;

  public static final String UPDATE_FB_CONFIG =
      """
      UPDATE fb_config
      SET app_id = ?,
          app_secret = ?,
          send_app_secret = ?
      WHERE tenant_id = ?
      """;

  public static final String DELETE_FB_CONFIG =
      """
      DELETE FROM fb_config
      WHERE tenant_id = ?
      """;
}
