package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_SMS_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.SmsConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.SmsConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateSmsConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateSmsConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class SmsConfigService {
  private final SmsConfigDao smsConfigDao;
  private final ChangelogService changelogService;
  private final MysqlClient mysqlClient;

  public Single<SmsConfigModel> createSmsConfig(
      String tenantId, CreateSmsConfigRequestDto requestDto) {
    SmsConfigModel smsConfig = mapToSmsConfigModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                smsConfigDao
                    .createSmsConfig(client, tenantId, smsConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    CONFIG_TYPE_SMS_CONFIG,
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .toMaybe())
        .switchIfEmpty(
            Single.<SmsConfigModel>error(
                INTERNAL_SERVER_ERROR.getCustomException("Failed to create SMS config")));
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
              SmsConfigModel updatedConfig = mergeSmsConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          smsConfigDao
                              .updateSmsConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_SMS_CONFIG,
                                      OPERATION_UPDATE,
                                      oldConfig,
                                      updatedConfig,
                                      tenantId))
                              .andThen(Single.just(updatedConfig))
                              .toMaybe())
                  .switchIfEmpty(
                      Single.<SmsConfigModel>error(
                          INTERNAL_SERVER_ERROR.getCustomException("Failed to update SMS config")));
            });
  }

  public Completable deleteSmsConfig(String tenantId) {
    return smsConfigDao
        .getSmsConfig(tenantId)
        .switchIfEmpty(Single.error(SMS_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            smsConfigDao
                                .deleteSmsConfig(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(
                                            SMS_CONFIG_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_SMS_CONFIG,
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .toMaybe())
                    .ignoreElement());
  }

  private SmsConfigModel mapToSmsConfigModel(CreateSmsConfigRequestDto requestDto) {
    return SmsConfigModel.builder()
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), DEFAULT_IS_SSL_ENABLED))
        .host(requestDto.getHost())
        .port(requestDto.getPort())
        .sendSmsPath(requestDto.getSendSmsPath())
        .templateName(requestDto.getTemplateName())
        .templateParams(requestDto.getTemplateParams())
        .build();
  }

  private SmsConfigModel mergeSmsConfig(
      UpdateSmsConfigRequestDto requestDto, SmsConfigModel oldConfig) {
    return SmsConfigModel.builder()
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), oldConfig.getIsSslEnabled()))
        .host(coalesce(requestDto.getHost(), oldConfig.getHost()))
        .port(coalesce(requestDto.getPort(), oldConfig.getPort()))
        .sendSmsPath(coalesce(requestDto.getSendSmsPath(), oldConfig.getSendSmsPath()))
        .templateName(coalesce(requestDto.getTemplateName(), oldConfig.getTemplateName()))
        .templateParams(coalesce(requestDto.getTemplateParams(), oldConfig.getTemplateParams()))
        .build();
  }
}
