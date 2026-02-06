package com.dreamsportslabs.guardian.config.tenant;

import lombok.Data;

@Data
public class PasswordPinBlockConfig {

  private Integer attemptsAllowed = 5;
  private Integer attemptsWindowSeconds = 86400;
  private Integer blockIntervalSeconds = 86400;
}
