package com.dreamsportslabs.guardian.dao.model;

import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_COOKIE_HTTP_ONLY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_COOKIE_SECURE;
import static com.dreamsportslabs.guardian.utils.Utils.JsonToStringDeserializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Setter
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenConfigModel {
  private String tenantId;
  private String algorithm;
  private String issuer;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String rsaKeys;

  private Integer accessTokenExpiry;
  private Integer refreshTokenExpiry;
  private Integer idTokenExpiry;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String idTokenClaims;

  private String cookieSameSite;
  private String cookieDomain;
  private String cookiePath;

  @Builder.Default private Boolean cookieSecure = DEFAULT_COOKIE_SECURE;

  @Builder.Default private Boolean cookieHttpOnly = DEFAULT_COOKIE_HTTP_ONLY;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String accessTokenClaims;
}
