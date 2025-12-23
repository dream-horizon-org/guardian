package com.dreamsportslabs.guardian.dao.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class CredentialsModel {
  private Long id;
  private String tenantId;
  private String clientId;
  private String userId;
  private String deviceId;
  private String platform;
  private String credentialId;
  private String publicKey;
  private String bindingType;
  private Integer alg;
  private Long signCount;
  private String aaguid;
  private Boolean isActive;
  private Boolean firstUseComplete;
}
