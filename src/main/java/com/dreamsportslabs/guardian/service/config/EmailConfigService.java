package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_EMAIL_CONFIG_PORT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.EmailConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.EmailConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateEmailConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateEmailConfigRequestDto;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class EmailConfigService {
  private final EmailConfigDao emailConfigDao;
  private final ChangelogService changelogService;
  private final MysqlClient mysqlClient;
  private final TenantCache tenantCache;

  public Single<EmailConfigModel> createEmailConfig(
      String tenantId, CreateEmailConfigRequestDto requestDto) {
    EmailConfigModel emailConfig = mapToEmailConfigModel(requestDto);
    return mysqlClient
        .getWriterPool()
        .rxWithTransaction(
            client ->
                emailConfigDao
                    .createEmailConfig(client, tenantId, emailConfig)
                    .flatMap(
                        createdConfig ->
                            changelogService
                                .logConfigChange(
                                    client,
                                    tenantId,
                                    CONFIG_TYPE_EMAIL_CONFIG,
                                    OPERATION_INSERT,
                                    null,
                                    createdConfig,
                                    tenantId)
                                .andThen(Single.just(createdConfig)))
                    .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
                    .toMaybe())
        .switchIfEmpty(
            Single.error(
                INTERNAL_SERVER_ERROR.getCustomException("Failed to create email config")));
  }

  public Single<EmailConfigModel> getEmailConfig(String tenantId) {
    return emailConfigDao
        .getEmailConfig(tenantId)
        .switchIfEmpty(Single.error(EMAIL_CONFIG_NOT_FOUND.getException()));
  }

  public Single<EmailConfigModel> updateEmailConfig(
      String tenantId, UpdateEmailConfigRequestDto requestDto) {
    return emailConfigDao
        .getEmailConfig(tenantId)
        .switchIfEmpty(Single.error(EMAIL_CONFIG_NOT_FOUND.getException()))
        .flatMap(
            oldConfig -> {
              EmailConfigModel updatedConfig = mergeEmailConfig(requestDto, oldConfig);
              return mysqlClient
                  .getWriterPool()
                  .rxWithTransaction(
                      client ->
                          emailConfigDao
                              .updateEmailConfig(client, tenantId, updatedConfig)
                              .andThen(
                                  changelogService.logConfigChange(
                                      client,
                                      tenantId,
                                      CONFIG_TYPE_EMAIL_CONFIG,
                                      OPERATION_UPDATE,
                                      oldConfig,
                                      updatedConfig,
                                      tenantId))
                              .andThen(Single.just(updatedConfig))
                              .doOnSuccess(config -> tenantCache.invalidateCache(tenantId))
                              .toMaybe())
                  .switchIfEmpty(
                      Single.error(
                          INTERNAL_SERVER_ERROR.getCustomException(
                              "Failed to update email config")));
            });
  }

  public Completable deleteEmailConfig(String tenantId) {
    return emailConfigDao
        .getEmailConfig(tenantId)
        .switchIfEmpty(Single.error(EMAIL_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                mysqlClient
                    .getWriterPool()
                    .rxWithTransaction(
                        client ->
                            emailConfigDao
                                .deleteEmailConfig(client, tenantId)
                                .flatMapCompletable(
                                    deleted -> {
                                      if (!deleted) {
                                        return Completable.error(
                                            EMAIL_CONFIG_NOT_FOUND.getException());
                                      }
                                      return changelogService.logConfigChange(
                                          client,
                                          tenantId,
                                          CONFIG_TYPE_EMAIL_CONFIG,
                                          OPERATION_DELETE,
                                          oldConfig,
                                          null,
                                          tenantId);
                                    })
                                .doOnComplete(() -> tenantCache.invalidateCache(tenantId))
                                .toMaybe())
                    .ignoreElement());
  }

  private EmailConfigModel mapToEmailConfigModel(CreateEmailConfigRequestDto requestDto) {
    return EmailConfigModel.builder()
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), DEFAULT_IS_SSL_ENABLED))
        .host(requestDto.getHost())
        .port(coalesce(requestDto.getPort(), DEFAULT_EMAIL_CONFIG_PORT))
        .sendEmailPath(requestDto.getSendEmailPath())
        .templateName(requestDto.getTemplateName())
        .templateParams(requestDto.getTemplateParams())
        .build();
  }

  private EmailConfigModel mergeEmailConfig(
      UpdateEmailConfigRequestDto requestDto, EmailConfigModel oldConfig) {
    return EmailConfigModel.builder()
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), oldConfig.getIsSslEnabled()))
        .host(coalesce(requestDto.getHost(), oldConfig.getHost()))
        .port(coalesce(requestDto.getPort(), oldConfig.getPort()))
        .sendEmailPath(coalesce(requestDto.getSendEmailPath(), oldConfig.getSendEmailPath()))
        .templateName(coalesce(requestDto.getTemplateName(), oldConfig.getTemplateName()))
        .templateParams(coalesce(requestDto.getTemplateParams(), oldConfig.getTemplateParams()))
        .build();
  }
}
