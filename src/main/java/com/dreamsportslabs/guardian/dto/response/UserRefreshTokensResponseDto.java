package com.dreamsportslabs.guardian.dto.response;

import com.dreamsportslabs.guardian.dao.model.UserRefreshTokenModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserRefreshTokensResponseDto {
  private List<UserRefreshTokenModel> refreshTokens;

  private int totalCount;
}
