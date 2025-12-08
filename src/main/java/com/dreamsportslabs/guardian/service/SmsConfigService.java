package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_CONFIG_NOT_FOUND;

import com.dreamsportslabs.guardian.dao.SmsConfigDao;
import com.dreamsportslabs.guardian.dao.model.SmsConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateSmsConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateSmsConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class SmsConfigService {
  private final SmsConfigDao smsConfigDao;

  public Single<SmsConfigModel> createSmsConfig(CreateSmsConfigRequestDto requestDto) {
    SmsConfigModel smsConfigModel =
        SmsConfigModel.builder()
            .tenantId(requestDto.getTenantId())
            .isSslEnabled(
                requestDto.getIsSslEnabled() != null ? requestDto.getIsSslEnabled() : false)
            .host(requestDto.getHost())
            .port(requestDto.getPort())
            .sendSmsPath(requestDto.getSendSmsPath())
            .templateName(requestDto.getTemplateName())
            .templateParams(requestDto.getTemplateParams())
            .build();

    return smsConfigDao.createSmsConfig(smsConfigModel);
  }

  public Single<SmsConfigModel> getSmsConfig(String tenantId) {
    return smsConfigDao
        .getSmsConfig(tenantId)
        .switchIfEmpty(Single.error(SMS_CONFIG_NOT_FOUND.getException()));
  }

  public Single<SmsConfigModel> updateSmsConfig(
      String tenantId, UpdateSmsConfigRequestDto requestDto) {
    return smsConfigDao
        .getSmsConfig(tenantId)
        .switchIfEmpty(Single.error(SMS_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            existing ->
                smsConfigDao.updateSmsConfig(tenantId, requestDto).andThen(getSmsConfig(tenantId)));
  }

  public Completable deleteSmsConfig(String tenantId) {
    return smsConfigDao
        .getSmsConfig(tenantId)
        .switchIfEmpty(Single.error(SMS_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            existing ->
                smsConfigDao
                    .deleteSmsConfig(tenantId)
                    .filter(deleted -> deleted)
                    .switchIfEmpty(
                        Single.error(
                            INTERNAL_SERVER_ERROR.getCustomException(
                                "Failed to delete SMS config")))
                    .ignoreElement());
  }
}
