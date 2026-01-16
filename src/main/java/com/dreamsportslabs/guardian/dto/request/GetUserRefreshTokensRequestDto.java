package com.dreamsportslabs.guardian.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetUserRefreshTokensRequestDto {
  @JsonProperty("client_id")
  private String clientId;
}
