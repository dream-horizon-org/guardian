package com.dreamsportslabs.guardian.dao.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
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

  private Boolean cookieSecure;

  private Boolean cookieHttpOnly;

  @JsonDeserialize(using = JsonToStringDeserializer.class)
  private String accessTokenClaims;

  private static class JsonToStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      if (node.isNull()) {
        return null;
      }
      if (node.isTextual()) {
        return node.asText();
      }
      return node.toString();
    }
  }
}
