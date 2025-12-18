package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_OTP_MOCKED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_LENGTH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_RESEND_INTERVAL;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_VALIDITY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_RESEND_LIMIT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TRY_LIMIT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OTP_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.OtpConfigDao;
import com.dreamsportslabs.guardian.dao.model.OtpConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateOtpConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOtpConfigRequestDto;
import com.dreamsportslabs.guardian.utils.Utils;
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

  public Single<OtpConfigModel> createOtpConfig(CreateOtpConfigRequestDto requestDto) {
    OtpConfigModel otpConfig = buildOtpConfigFromCreateRequest(requestDto);
    return otpConfigDao
        .createOtpConfig(otpConfig)
        .flatMap(
            createdConfig ->
                changelogService
                    .logConfigChange(
                        createdConfig.getTenantId(),
                        CONFIG_TYPE_OTP_CONFIG,
                        OPERATION_INSERT,
                        null,
                        createdConfig,
                        createdConfig.getTenantId())
                    .andThen(Single.just(createdConfig)));
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
              OtpConfigModel updatedConfig = mergeOtpConfig(tenantId, requestDto, oldConfig);
              return otpConfigDao
                  .updateOtpConfig(updatedConfig)
                  .andThen(
                      changelogService
                          .logConfigChange(
                              tenantId,
                              CONFIG_TYPE_OTP_CONFIG,
                              OPERATION_UPDATE,
                              oldConfig,
                              updatedConfig,
                              tenantId)
                          .andThen(Single.just(updatedConfig)));
            });
  }

  public Completable deleteOtpConfig(String tenantId) {
    return otpConfigDao
        .getOtpConfig(tenantId)
        .switchIfEmpty(Single.error(OTP_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                otpConfigDao
                    .deleteOtpConfig(tenantId)
                    .flatMapCompletable(
                        deleted -> {
                          if (!deleted) {
                            return Completable.error(OTP_CONFIG_NOT_FOUND.getException());
                          }
                          return changelogService.logConfigChange(
                              tenantId,
                              CONFIG_TYPE_OTP_CONFIG,
                              OPERATION_DELETE,
                              oldConfig,
                              null,
                              tenantId);
                        }));
  }

  private OtpConfigModel buildOtpConfigFromCreateRequest(CreateOtpConfigRequestDto requestDto) {
    return OtpConfigModel.builder()
        .tenantId(requestDto.getTenantId())
        .isOtpMocked(coalesce(requestDto.getIsOtpMocked(), DEFAULT_IS_OTP_MOCKED))
        .otpLength(coalesce(requestDto.getOtpLength(), DEFAULT_OTP_LENGTH))
        .tryLimit(coalesce(requestDto.getTryLimit(), DEFAULT_TRY_LIMIT))
        .resendLimit(coalesce(requestDto.getResendLimit(), DEFAULT_RESEND_LIMIT))
        .otpResendInterval(coalesce(requestDto.getOtpResendInterval(), DEFAULT_OTP_RESEND_INTERVAL))
        .otpValidity(coalesce(requestDto.getOtpValidity(), DEFAULT_OTP_VALIDITY))
        .whitelistedInputs(Utils.encodeWhitelistedInputs(requestDto.getWhitelistedInputs()))
        .build();
  }

  private OtpConfigModel mergeOtpConfig(
      String tenantId, UpdateOtpConfigRequestDto requestDto, OtpConfigModel oldConfig) {
    return OtpConfigModel.builder()
        .tenantId(tenantId)
        .isOtpMocked(coalesce(requestDto.getIsOtpMocked(), oldConfig.getIsOtpMocked()))
        .otpLength(coalesce(requestDto.getOtpLength(), oldConfig.getOtpLength()))
        .tryLimit(coalesce(requestDto.getTryLimit(), oldConfig.getTryLimit()))
        .resendLimit(coalesce(requestDto.getResendLimit(), oldConfig.getResendLimit()))
        .otpResendInterval(
            coalesce(requestDto.getOtpResendInterval(), oldConfig.getOtpResendInterval()))
        .otpValidity(coalesce(requestDto.getOtpValidity(), oldConfig.getOtpValidity()))
        .whitelistedInputs(
            requestDto.getWhitelistedInputs() != null
                ? Utils.encodeWhitelistedInputs(requestDto.getWhitelistedInputs())
                : oldConfig.getWhitelistedInputs())
        .build();
  }
}
