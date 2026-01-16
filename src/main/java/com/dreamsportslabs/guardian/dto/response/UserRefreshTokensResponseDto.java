package com.dreamsportslabs.guardian.dto.response;

import com.dreamsportslabs.guardian.dao.model.UserRefreshTokenModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserRefreshTokensResponseDto {
  @JsonProperty("refresh_tokens")
  private List<UserRefreshTokenModel> refreshTokens;

  @JsonProperty("total_count")
  private int totalCount;
}
