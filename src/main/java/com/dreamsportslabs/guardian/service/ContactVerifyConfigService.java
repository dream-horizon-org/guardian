package com.dreamsportslabs.guardian.service;

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
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.ContactVerifyConfigDao;
import com.dreamsportslabs.guardian.dao.model.ContactVerifyConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateContactVerifyConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateContactVerifyConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ContactVerifyConfigService {
  private final ContactVerifyConfigDao contactVerifyConfigDao;
  private final ChangelogService changelogService;

  public Single<ContactVerifyConfigModel> createContactVerifyConfig(
      CreateContactVerifyConfigRequestDto requestDto) {
    ContactVerifyConfigModel contactVerifyConfig =
        buildContactVerifyConfigFromCreateRequest(requestDto);
    return contactVerifyConfigDao
        .createContactVerifyConfig(contactVerifyConfig)
        .flatMap(
            createdConfig ->
                changelogService
                    .logConfigChange(
                        createdConfig.getTenantId(),
                        CONFIG_TYPE_CONTACT_VERIFY_CONFIG,
                        OPERATION_INSERT,
                        null,
                        createdConfig,
                        createdConfig.getTenantId())
                    .andThen(Single.just(createdConfig)));
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
                  mergeContactVerifyConfig(tenantId, requestDto, oldConfig);
              return contactVerifyConfigDao
                  .updateContactVerifyConfig(updatedConfig)
                  .andThen(getContactVerifyConfig(tenantId))
                  .flatMap(
                      newConfig ->
                          changelogService
                              .logConfigChange(
                                  tenantId,
                                  CONFIG_TYPE_CONTACT_VERIFY_CONFIG,
                                  OPERATION_UPDATE,
                                  oldConfig,
                                  newConfig,
                                  tenantId)
                              .andThen(Single.just(newConfig)));
            });
  }

  public Completable deleteContactVerifyConfig(String tenantId) {
    return contactVerifyConfigDao
        .getContactVerifyConfig(tenantId)
        .switchIfEmpty(Single.error(CONTACT_VERIFY_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                contactVerifyConfigDao
                    .deleteContactVerifyConfig(tenantId)
                    .ignoreElement()
                    .andThen(
                        changelogService.logConfigChange(
                            tenantId,
                            CONFIG_TYPE_CONTACT_VERIFY_CONFIG,
                            OPERATION_DELETE,
                            oldConfig,
                            null,
                            tenantId)));
  }

  private ContactVerifyConfigModel buildContactVerifyConfigFromCreateRequest(
      CreateContactVerifyConfigRequestDto requestDto) {
    return ContactVerifyConfigModel.builder()
        .tenantId(requestDto.getTenantId())
        .isOtpMocked(coalesce(requestDto.getIsOtpMocked(), DEFAULT_IS_OTP_MOCKED))
        .otpLength(coalesce(requestDto.getOtpLength(), DEFAULT_OTP_LENGTH))
        .tryLimit(coalesce(requestDto.getTryLimit(), DEFAULT_TRY_LIMIT))
        .resendLimit(coalesce(requestDto.getResendLimit(), DEFAULT_RESEND_LIMIT))
        .otpResendInterval(coalesce(requestDto.getOtpResendInterval(), DEFAULT_OTP_RESEND_INTERVAL))
        .otpValidity(coalesce(requestDto.getOtpValidity(), DEFAULT_OTP_VALIDITY))
        .whitelistedInputs(encodeWhitelistedInputs(requestDto.getWhitelistedInputs()))
        .build();
  }

  private ContactVerifyConfigModel mergeContactVerifyConfig(
      String tenantId,
      UpdateContactVerifyConfigRequestDto requestDto,
      ContactVerifyConfigModel oldConfig) {
    return ContactVerifyConfigModel.builder()
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
                ? encodeWhitelistedInputs(requestDto.getWhitelistedInputs())
                : oldConfig.getWhitelistedInputs())
        .build();
  }

  private String encodeWhitelistedInputs(java.util.List<String> whitelistedInputs) {
    JsonObject jsonObject = new JsonObject();
    if (whitelistedInputs != null) {
      for (String input : whitelistedInputs) {
        jsonObject.put(input, true);
      }
    }
    return jsonObject.encode();
  }
}
