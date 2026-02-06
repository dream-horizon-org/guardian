package com.dreamsportslabs.guardian.config.tenant;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class PasswordPinBlockConfig {

  public static final int DEFAULT_ATTEMPTS_ALLOWED = 5;
  public static final int DEFAULT_ATTEMPTS_WINDOW_SECONDS = 86400;
  public static final int DEFAULT_BLOCK_INTERVAL_SECONDS = 86400;

  @Builder.Default int attemptsAllowed = DEFAULT_ATTEMPTS_ALLOWED;
  @Builder.Default int attemptsWindowSeconds = DEFAULT_ATTEMPTS_WINDOW_SECONDS;
  @Builder.Default int blockIntervalSeconds = DEFAULT_BLOCK_INTERVAL_SECONDS;
}
