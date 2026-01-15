package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_OTP_MOCKED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_LENGTH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_RESEND_INTERVAL;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_SEND_BLOCK_SECONDS;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_SEND_WINDOW_MAX_COUNT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_SEND_WINDOW_SECONDS;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_VALIDITY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_RESEND_LIMIT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TRY_LIMIT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OTP_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.OtpConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.OtpConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateOtpConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOtpConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class OtpConfigService {
  private final OtpConfigDao otpConfigDao;
  private final ChangelogService changelogService;
  private final MysqlClient mysqlClient;

  public Single<OtpConfigModel> createOtpConfig(
      String tenantId, CreateOtpConfigRequestDto requestDto) {
    OtpConfigModel otpConfig = mapToOtpConfigModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                otpConfigDao
                    .createOtpConfig(client, tenantId, otpConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    CONFIG_TYPE_OTP_CONFIG,
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .switchIfEmpty(
            Single.<OtpConfigModel>error(
                INTERNAL_SERVER_ERROR.getCustomException("Failed to create OTP config")));
  }

  public Single<OtpConfigModel> getOtpConfig(String tenantId) {
    return otpConfigDao
        .getOtpConfig(tenantId)
        .switchIfEmpty(Single.error(OTP_CONFIG_NOT_FOUND.getException()));
  }

  public Single<OtpConfigModel> updateOtpConfig(
      String tenantId, UpdateOtpConfigRequestDto requestDto) {
    return otpConfigDao
        .getOtpConfig(tenantId)
        .switchIfEmpty(Single.error(OTP_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              OtpConfigModel updatedConfig = mergeOtpConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          otpConfigDao
                              .updateOtpConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_OTP_CONFIG,
                                      OPERATION_UPDATE,
                                      oldConfig,
                                      updatedConfig,
                                      tenantId))
                              .andThen(Single.just(updatedConfig))
                              .toMaybe())
                  .switchIfEmpty(
                      Single.<OtpConfigModel>error(
                          INTERNAL_SERVER_ERROR.getCustomException("Failed to update OTP config")));
            });
  }

  public Completable deleteOtpConfig(String tenantId) {
    return otpConfigDao
        .getOtpConfig(tenantId)
        .switchIfEmpty(Single.error(OTP_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            otpConfigDao
                                .deleteOtpConfig(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(
                                            OTP_CONFIG_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_OTP_CONFIG,
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .toMaybe())
                    .ignoreElement());
  }

  private OtpConfigModel mapToOtpConfigModel(CreateOtpConfigRequestDto requestDto) {
    return OtpConfigModel.builder()
        .isOtpMocked(coalesce(requestDto.getIsOtpMocked(), DEFAULT_IS_OTP_MOCKED))
        .otpLength(coalesce(requestDto.getOtpLength(), DEFAULT_OTP_LENGTH))
        .tryLimit(coalesce(requestDto.getTryLimit(), DEFAULT_TRY_LIMIT))
        .resendLimit(coalesce(requestDto.getResendLimit(), DEFAULT_RESEND_LIMIT))
        .otpResendInterval(coalesce(requestDto.getOtpResendInterval(), DEFAULT_OTP_RESEND_INTERVAL))
        .otpValidity(coalesce(requestDto.getOtpValidity(), DEFAULT_OTP_VALIDITY))
        .otpSendWindowSeconds(
            coalesce(requestDto.getOtpSendWindowSeconds(), DEFAULT_OTP_SEND_WINDOW_SECONDS))
        .otpSendWindowMaxCount(
            coalesce(requestDto.getOtpSendWindowMaxCount(), DEFAULT_OTP_SEND_WINDOW_MAX_COUNT))
        .otpSendBlockSeconds(
            coalesce(requestDto.getOtpSendBlockSeconds(), DEFAULT_OTP_SEND_BLOCK_SECONDS))
        .whitelistedInputs(requestDto.getWhitelistedInputs())
        .build();
  }

  private OtpConfigModel mergeOtpConfig(
      UpdateOtpConfigRequestDto requestDto, OtpConfigModel oldConfig) {
    return OtpConfigModel.builder()
        .isOtpMocked(coalesce(requestDto.getIsOtpMocked(), oldConfig.getIsOtpMocked()))
        .otpLength(coalesce(requestDto.getOtpLength(), oldConfig.getOtpLength()))
        .tryLimit(coalesce(requestDto.getTryLimit(), oldConfig.getTryLimit()))
        .resendLimit(coalesce(requestDto.getResendLimit(), oldConfig.getResendLimit()))
        .otpResendInterval(
            coalesce(requestDto.getOtpResendInterval(), oldConfig.getOtpResendInterval()))
        .otpValidity(coalesce(requestDto.getOtpValidity(), oldConfig.getOtpValidity()))
        .otpSendWindowSeconds(
            coalesce(requestDto.getOtpSendWindowSeconds(), oldConfig.getOtpSendWindowSeconds()))
        .otpSendWindowMaxCount(
            coalesce(requestDto.getOtpSendWindowMaxCount(), oldConfig.getOtpSendWindowMaxCount()))
        .otpSendBlockSeconds(
            coalesce(requestDto.getOtpSendBlockSeconds(), oldConfig.getOtpSendBlockSeconds()))
        .whitelistedInputs(
            coalesce(requestDto.getWhitelistedInputs(), oldConfig.getWhitelistedInputs()))
        .build();
  }
}
