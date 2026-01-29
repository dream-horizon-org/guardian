package com.dreamsportslabs.guardian.dto.response;

import com.dreamsportslabs.guardian.dao.model.UserRefreshTokenModel;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserRefreshTokensResponseDto {
  private List<UserRefreshTokenModel> refreshTokens;
  private Long totalCount;
}
