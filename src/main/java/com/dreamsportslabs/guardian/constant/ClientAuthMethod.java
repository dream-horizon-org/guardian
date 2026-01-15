package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ClientAuthMethod {
  BASIC("client_secret_basic"),
  POST("client_secret_post");

  private final String value;

  ClientAuthMethod(String clientAuthMethod) {
    this.value = clientAuthMethod;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  @JsonCreator
  public static ClientAuthMethod fromString(String value) {
    if (value == null) {
      return null;
    }
    for (ClientAuthMethod method : ClientAuthMethod.values()) {
      if (method.value.equals(value)) {
        return method;
      }
    }
    return null;
  }
}
