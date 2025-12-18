package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_SMS_CONFIG_PORT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.SmsConfigDao;
import com.dreamsportslabs.guardian.dao.model.SmsConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateSmsConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateSmsConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class SmsConfigService {
  private final SmsConfigDao smsConfigDao;
  private final ChangelogService changelogService;

  public Single<SmsConfigModel> createSmsConfig(CreateSmsConfigRequestDto requestDto) {
    SmsConfigModel smsConfig = buildSmsConfigFromCreateRequest(requestDto);
    return smsConfigDao
        .createSmsConfig(smsConfig)
        .flatMap(
            createdConfig ->
                changelogService
                    .logConfigChange(
                        createdConfig.getTenantId(),
                        CONFIG_TYPE_SMS_CONFIG,
                        OPERATION_INSERT,
                        null,
                        createdConfig,
                        createdConfig.getTenantId())
                    .andThen(Single.just(createdConfig)));
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
            oldConfig -> {
              SmsConfigModel updatedConfig = mergeSmsConfig(tenantId, requestDto, oldConfig);
              return smsConfigDao
                  .updateSmsConfig(updatedConfig)
                  .andThen(
                      changelogService
                          .logConfigChange(
                              tenantId,
                              CONFIG_TYPE_SMS_CONFIG,
                              OPERATION_UPDATE,
                              oldConfig,
                              updatedConfig,
                              tenantId)
                          .andThen(Single.just(updatedConfig)));
            });
  }

  public Completable deleteSmsConfig(String tenantId) {
    return smsConfigDao
        .getSmsConfig(tenantId)
        .switchIfEmpty(Single.error(SMS_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                smsConfigDao
                    .deleteSmsConfig(tenantId)
                    .flatMapCompletable(
                        deleted -> {
                          if (!deleted) {
                            return Completable.error(SMS_CONFIG_NOT_FOUND.getException());
                          }
                          return changelogService.logConfigChange(
                              tenantId,
                              CONFIG_TYPE_SMS_CONFIG,
                              OPERATION_DELETE,
                              oldConfig,
                              null,
                              tenantId);
                        }));
  }

  private SmsConfigModel buildSmsConfigFromCreateRequest(CreateSmsConfigRequestDto requestDto) {
    return SmsConfigModel.builder()
        .tenantId(requestDto.getTenantId())
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), DEFAULT_IS_SSL_ENABLED))
        .host(requestDto.getHost())
        .port(coalesce(requestDto.getPort(), DEFAULT_SMS_CONFIG_PORT))
        .sendSmsPath(requestDto.getSendSmsPath())
        .templateName(requestDto.getTemplateName())
        .templateParams(encodeTemplateParams(requestDto.getTemplateParams()))
        .build();
  }

  private SmsConfigModel mergeSmsConfig(
      String tenantId, UpdateSmsConfigRequestDto requestDto, SmsConfigModel oldConfig) {
    return SmsConfigModel.builder()
        .tenantId(tenantId)
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), oldConfig.getIsSslEnabled()))
        .host(coalesce(requestDto.getHost(), oldConfig.getHost()))
        .port(coalesce(requestDto.getPort(), oldConfig.getPort()))
        .sendSmsPath(coalesce(requestDto.getSendSmsPath(), oldConfig.getSendSmsPath()))
        .templateName(coalesce(requestDto.getTemplateName(), oldConfig.getTemplateName()))
        .templateParams(
            requestDto.getTemplateParams() != null
                ? encodeTemplateParams(requestDto.getTemplateParams())
                : oldConfig.getTemplateParams())
        .build();
  }

  private String encodeTemplateParams(java.util.Map<String, String> templateParams) {
    return JsonObject.mapFrom(templateParams).encode();
  }
}
