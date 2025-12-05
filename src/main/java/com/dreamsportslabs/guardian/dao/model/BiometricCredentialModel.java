package com.dreamsportslabs.guardian.dao.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BiometricCredentialModel {
  private Long id;
  private String tenantId;
  private String clientId;
  private String userId;
  private String credentialId;
  private String publicKey;
  private String bindingType;
  private Integer alg;
  private Long signCount;
  private String aaguid;
  private Boolean isActive;
  private Boolean firstUseComplete;
}
