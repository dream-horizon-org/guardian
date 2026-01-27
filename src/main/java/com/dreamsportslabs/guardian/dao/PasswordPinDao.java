package com.dreamsportslabs.guardian.dao;

import static com.dreamsportslabs.guardian.constant.Constants.CACHE_KEY_PASSWORD_ATTEMPTS;
import static com.dreamsportslabs.guardian.constant.Constants.CACHE_KEY_PIN_ATTEMPTS;

import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.redis.client.Command;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.Request;
import io.vertx.rxjava3.redis.client.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class PasswordPinDao {
  private final Redis redisClient;

  public Single<Integer> getWrongAttemptsCount(
      String tenantId, String userId, BlockFlow blockFlow) {
    String attemptsCountKey = getAttemptsKey(tenantId, userId, blockFlow);

    return redisClient
        .rxSend(Request.cmd(Command.GET).arg(attemptsCountKey))
        .map(Response::toInteger)
        .switchIfEmpty(Single.just(0));
  }

  public Completable incrementWrongAttemptsCount(
      String tenantId, String userId, Integer ttlSeconds, BlockFlow blockFlow) {
    String redisKey = getAttemptsKey(tenantId, userId, blockFlow);

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

  public Completable deleteWrongAttemptsCount(String tenantId, String userId, BlockFlow blockFlow) {
    String redisKey = getAttemptsKey(tenantId, userId, blockFlow);
    return redisClient.rxSend(Request.cmd(Command.DEL).arg(redisKey)).ignoreElement();
  }

  private String getAttemptsKey(String tenantId, String userId, BlockFlow blockFlow) {
    String prefix =
        blockFlow == BlockFlow.PASSWORD ? CACHE_KEY_PASSWORD_ATTEMPTS : CACHE_KEY_PIN_ATTEMPTS;
    return prefix + "_" + tenantId + "_" + userId;
  }
}
