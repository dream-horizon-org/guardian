package com.dreamsportslabs.guardian.config.tenant;

import lombok.Data;

@Data
public class PasswordPinBlockConfig {

  private Integer attemptsAllowed;
  private Integer attemptsWindowSeconds;
  private Integer blockIntervalSeconds;
}
