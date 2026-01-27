package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.BlockFlow.MFA_SIGNIN_PASSWORD;
import static com.dreamsportslabs.guardian.constant.BlockFlow.MFA_SIGNIN_PIN;
import static com.dreamsportslabs.guardian.constant.Constants.CACHE_KEY_MFA_PASSWORD_ATTEMPTS;
import static com.dreamsportslabs.guardian.constant.Constants.CACHE_KEY_MFA_PIN_ATTEMPTS;

import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.redis.client.Command;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.Request;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MfaDao {
  private final Redis redisClient;

  public Single<Integer> getWrongAttemptsCount(
      String tenantId, String userId, String deviceId, BlockFlow blockFlow) {
    String redisKey = getAttemptsKey(tenantId, userId, deviceId, blockFlow);

    return redisClient
        .rxSend(Request.cmd(Command.GET).arg(redisKey))
        .map(response -> response.toInteger())
        .switchIfEmpty(Single.just(0));
  }

  public Completable incrementWrongAttemptsCount(
      String tenantId, String userId, String deviceId, Integer ttlSeconds, BlockFlow blockFlow) {
    String redisKey = getAttemptsKey(tenantId, userId, deviceId, blockFlow);

    return redisClient
        .rxSend(Request.cmd(Command.INCR).arg(redisKey))
        .flatMap(
            response -> {
              if (response.toInteger() == 1) {
                return redisClient.rxSend(
                    Request.cmd(Command.EXPIRE).arg(redisKey).arg(String.valueOf(ttlSeconds)));
              }
              return Maybe.just(response);
            })
        .ignoreElement();
  }

  public Completable deleteWrongAttemptsCount(
      String tenantId, String userId, String deviceId, BlockFlow blockFlow) {
    String redisKey = getAttemptsKey(tenantId, userId, deviceId, blockFlow);
    return redisClient.rxSend(Request.cmd(Command.DEL).arg(redisKey)).ignoreElement();
  }

  private String getAttemptsKey(
      String tenantId, String userId, String deviceId, BlockFlow blockFlow) {
    String prefix =
        blockFlow == MFA_SIGNIN_PASSWORD
            ? CACHE_KEY_MFA_PASSWORD_ATTEMPTS
            : blockFlow == MFA_SIGNIN_PIN ? CACHE_KEY_MFA_PIN_ATTEMPTS : null;
    if (prefix == null) {
      throw new IllegalArgumentException(
          "Attempts counter only supported for MFA_SIGNIN_PASSWORD and MFA_SIGNIN_PIN");
    }
    return prefix + "_" + tenantId + "_" + userId + "_" + deviceId;
  }
}
