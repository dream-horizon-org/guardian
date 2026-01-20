package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_OTP_MOCKED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_LENGTH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_RESEND_INTERVAL;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_OTP_VALIDITY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_RESEND_LIMIT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TRY_LIMIT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CONTACT_VERIFY_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.ContactVerifyConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.ContactVerifyConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateContactVerifyConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateContactVerifyConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ContactVerifyConfigService {
  private final ContactVerifyConfigDao contactVerifyConfigDao;
  private final ChangelogService changelogService;
  private final MysqlClient mysqlClient;
  private final TenantCache tenantCache;

  public Single<ContactVerifyConfigModel> createContactVerifyConfig(
      String tenantId, CreateContactVerifyConfigRequestDto requestDto) {
    ContactVerifyConfigModel contactVerifyConfig = mapToContactVerifyConfigModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                contactVerifyConfigDao
                    .createContactVerifyConfig(client, tenantId, contactVerifyConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    CONFIG_TYPE_CONTACT_VERIFY_CONFIG,
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
        .switchIfEmpty(
            Single.error(
                INTERNAL_SERVER_ERROR.getCustomException(
                    "Failed to create contact verify config")));
  }

  public Single<ContactVerifyConfigModel> getContactVerifyConfig(String tenantId) {
    return contactVerifyConfigDao
        .getContactVerifyConfig(tenantId)
        .switchIfEmpty(Single.error(CONTACT_VERIFY_CONFIG_NOT_FOUND.getException()));
  }

  public Single<ContactVerifyConfigModel> updateContactVerifyConfig(
      String tenantId, UpdateContactVerifyConfigRequestDto requestDto) {
    return contactVerifyConfigDao
        .getContactVerifyConfig(tenantId)
        .switchIfEmpty(Single.error(CONTACT_VERIFY_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              ContactVerifyConfigModel updatedConfig =
                  mergeContactVerifyConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          contactVerifyConfigDao
                              .updateContactVerifyConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_CONTACT_VERIFY_CONFIG,
                                      OPERATION_UPDATE,
                                      oldConfig,
                                      updatedConfig,
                                      tenantId))
                              .andThen(Single.just(updatedConfig))
                              .toMaybe())
                  .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
                  .switchIfEmpty(
                      Single.error(
                          INTERNAL_SERVER_ERROR.getCustomException(
                              "Failed to update contact verify config")));
            });
  }

  public Completable deleteContactVerifyConfig(String tenantId) {
    return contactVerifyConfigDao
        .getContactVerifyConfig(tenantId)
        .switchIfEmpty(Single.error(CONTACT_VERIFY_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            contactVerifyConfigDao
                                .deleteContactVerifyConfig(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(
                                            CONTACT_VERIFY_CONFIG_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_CONTACT_VERIFY_CONFIG,
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .toMaybe())
                    .doOnComplete(() -> tenantCache.invalidateCache(tenantId))
                    .ignoreElement());
  }

  private ContactVerifyConfigModel mapToContactVerifyConfigModel(
      CreateContactVerifyConfigRequestDto requestDto) {
    return ContactVerifyConfigModel.builder()
        .isOtpMocked(coalesce(requestDto.getIsOtpMocked(), DEFAULT_IS_OTP_MOCKED))
        .otpLength(coalesce(requestDto.getOtpLength(), DEFAULT_OTP_LENGTH))
        .tryLimit(coalesce(requestDto.getTryLimit(), DEFAULT_TRY_LIMIT))
        .resendLimit(coalesce(requestDto.getResendLimit(), DEFAULT_RESEND_LIMIT))
        .otpResendInterval(coalesce(requestDto.getOtpResendInterval(), DEFAULT_OTP_RESEND_INTERVAL))
        .otpValidity(coalesce(requestDto.getOtpValidity(), DEFAULT_OTP_VALIDITY))
        .whitelistedInputs(coalesce(requestDto.getWhitelistedInputs(), new HashMap<>()))
        .build();
  }

  private ContactVerifyConfigModel mergeContactVerifyConfig(
      UpdateContactVerifyConfigRequestDto requestDto, ContactVerifyConfigModel oldConfig) {
    return ContactVerifyConfigModel.builder()
        .isOtpMocked(coalesce(requestDto.getIsOtpMocked(), oldConfig.getIsOtpMocked()))
        .otpLength(coalesce(requestDto.getOtpLength(), oldConfig.getOtpLength()))
        .tryLimit(coalesce(requestDto.getTryLimit(), oldConfig.getTryLimit()))
        .resendLimit(coalesce(requestDto.getResendLimit(), oldConfig.getResendLimit()))
        .otpResendInterval(
            coalesce(requestDto.getOtpResendInterval(), oldConfig.getOtpResendInterval()))
        .otpValidity(coalesce(requestDto.getOtpValidity(), oldConfig.getOtpValidity()))
        .whitelistedInputs(
            coalesce(requestDto.getWhitelistedInputs(), oldConfig.getWhitelistedInputs()))
        .build();
  }
}
