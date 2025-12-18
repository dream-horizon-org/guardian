package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_GUEST_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_ENCRYPTED;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GUEST_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.GuestConfigDao;
import com.dreamsportslabs.guardian.dao.model.GuestConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateGuestConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateGuestConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class GuestConfigService {
  private final GuestConfigDao guestConfigDao;
  private final ChangelogService changelogService;

  public Single<GuestConfigModel> createGuestConfig(CreateGuestConfigRequestDto requestDto) {
    GuestConfigModel guestConfig = buildGuestConfigFromCreateRequest(requestDto);
    return guestConfigDao
        .createGuestConfig(guestConfig)
        .flatMap(
            createdConfig ->
                changelogService
                    .logConfigChange(
                        createdConfig.getTenantId(),
                        CONFIG_TYPE_GUEST_CONFIG,
                        OPERATION_INSERT,
                        null,
                        createdConfig,
                        createdConfig.getTenantId())
                    .andThen(Single.just(createdConfig)));
  }

  public Single<GuestConfigModel> getGuestConfig(String tenantId) {
    return guestConfigDao
        .getGuestConfig(tenantId)
        .switchIfEmpty(Single.error(GUEST_CONFIG_NOT_FOUND.getException()));
  }

  public Single<GuestConfigModel> updateGuestConfig(
      String tenantId, UpdateGuestConfigRequestDto requestDto) {
    return guestConfigDao
        .getGuestConfig(tenantId)
        .switchIfEmpty(Single.error(GUEST_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              GuestConfigModel updatedConfig = mergeGuestConfig(tenantId, requestDto, oldConfig);
              return guestConfigDao
                  .updateGuestConfig(updatedConfig)
                  .andThen(
                      changelogService
                          .logConfigChange(
                              tenantId,
                              CONFIG_TYPE_GUEST_CONFIG,
                              OPERATION_UPDATE,
                              oldConfig,
                              updatedConfig,
                              tenantId)
                          .andThen(Single.just(updatedConfig)));
            });
  }

  public Completable deleteGuestConfig(String tenantId) {
    return guestConfigDao
        .getGuestConfig(tenantId)
        .switchIfEmpty(Single.error(GUEST_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                guestConfigDao
                    .deleteGuestConfig(tenantId)
                    .flatMapCompletable(
                        deleted -> {
                          if (!deleted) {
                            return Completable.error(GUEST_CONFIG_NOT_FOUND.getException());
                          }
                          return changelogService.logConfigChange(
                              tenantId,
                              CONFIG_TYPE_GUEST_CONFIG,
                              OPERATION_DELETE,
                              oldConfig,
                              null,
                              tenantId);
                        }));
  }

  private GuestConfigModel buildGuestConfigFromCreateRequest(
      CreateGuestConfigRequestDto requestDto) {
    return GuestConfigModel.builder()
        .tenantId(requestDto.getTenantId())
        .isEncrypted(coalesce(requestDto.getIsEncrypted(), DEFAULT_IS_ENCRYPTED))
        .secretKey(requestDto.getSecretKey())
        .allowedScopes(encodeJsonArray(requestDto.getAllowedScopes()))
        .build();
  }

  private GuestConfigModel mergeGuestConfig(
      String tenantId, UpdateGuestConfigRequestDto requestDto, GuestConfigModel oldConfig) {
    return GuestConfigModel.builder()
        .tenantId(tenantId)
        .isEncrypted(coalesce(requestDto.getIsEncrypted(), oldConfig.getIsEncrypted()))
        .secretKey(coalesce(requestDto.getSecretKey(), oldConfig.getSecretKey()))
        .allowedScopes(
            requestDto.getAllowedScopes() != null
                ? encodeJsonArray(requestDto.getAllowedScopes())
                : oldConfig.getAllowedScopes())
        .build();
  }

  private String encodeJsonArray(List<String> list) {
    if (list == null) {
      return "[]";
    }
    return new JsonArray(list).encode();
  }
}
