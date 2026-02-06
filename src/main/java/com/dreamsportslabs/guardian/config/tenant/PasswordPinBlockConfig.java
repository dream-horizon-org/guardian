package com.dreamsportslabs.guardian.config.tenant;

import lombok.Data;

@Data
public class PasswordPinBlockConfig {

  private int attemptsAllowed;
  private int attemptsWindowSeconds;
  private int blockIntervalSeconds;
}
