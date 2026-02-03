package com.dreamsportslabs.guardian.dao.query;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ChangelogQuery {

  public static final String LOG_CONFIG_CHANGE =
      """
            INSERT INTO config_changelog (tenant_id, config_type, operation_type, old_values, new_values, changed_by)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

  public static final String GET_CHANGELOG_BY_ID =
      """
            SELECT id, tenant_id, config_type, operation_type, changed_by, changed_at, old_values, new_values
            FROM config_changelog
            WHERE id = ?
            """;

  public static final String GET_CHANGELOG_BY_TENANT =
      """
            SELECT id, tenant_id, config_type, operation_type, changed_by, changed_at, old_values, new_values
            FROM config_changelog
            WHERE tenant_id = ?
            ORDER BY changed_at DESC
            LIMIT ? OFFSET ?
            """;

  public static final String COUNT_CHANGELOG_BY_TENANT =
      """
            SELECT COUNT(*) as total
            FROM config_changelog
            WHERE tenant_id = ?
            """;
}
