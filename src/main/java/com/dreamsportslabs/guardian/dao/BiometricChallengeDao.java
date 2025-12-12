package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.CACHE_KEY_BIOMETRIC_CHALLENGE;
import static com.dreamsportslabs.guardian.constant.Constants.EXPIRE_AT_REDIS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.dao.model.BiometricChallengeModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.redis.client.Command;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.Request;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BiometricChallengeDao {
  private final Redis redisClient;
  private final ObjectMapper objectMapper;

  @SneakyThrows
  public Single<BiometricChallengeModel> saveChallenge(
      BiometricChallengeModel model, String tenantId) {
    return redisClient
        .rxSend(
            Request.cmd(Command.SET)
                .arg(getCacheKey(tenantId, model.getState()))
                .arg(objectMapper.writeValueAsString(model))
                .arg(EXPIRE_AT_REDIS)
                .arg(model.getExpiry()))
        .onErrorResumeNext(err -> Maybe.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(response -> model)
        .toSingle();
  }

  public Maybe<BiometricChallengeModel> getChallenge(String state, String tenantId) {
    return redisClient
        .rxSend(Request.cmd(Command.GET).arg(getCacheKey(tenantId, state)))
        .filter(response -> response != null && response.toString() != null)
        .map(response -> objectMapper.readValue(response.toString(), BiometricChallengeModel.class))
        .onErrorResumeNext(
            err -> {
              log.error("Failed to deserialize challenge", err);
              return Maybe.error(INTERNAL_SERVER_ERROR.getException(err));
            });
  }

  public void deleteChallenge(String state, String tenantId) {
    redisClient.rxSend(Request.cmd(Command.DEL).arg(getCacheKey(tenantId, state))).subscribe();
  }

  private String getCacheKey(String tenantId, String state) {
    return CACHE_KEY_BIOMETRIC_CHALLENGE + "_" + tenantId + "_" + state;
  }
}
