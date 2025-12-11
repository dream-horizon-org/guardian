package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_EMAIL_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_DELETE;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.dao.EmailConfigDao;
import com.dreamsportslabs.guardian.dao.model.EmailConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateEmailConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateEmailConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class EmailConfigService {
  private final EmailConfigDao emailConfigDao;
  private final ChangelogService changelogService;

  public Single<EmailConfigModel> createEmailConfig(CreateEmailConfigRequestDto requestDto) {
    EmailConfigModel emailConfig = buildEmailConfigFromCreateRequest(requestDto);
    return emailConfigDao
        .createEmailConfig(emailConfig)
        .flatMap(
            createdConfig ->
                changelogService
                    .logConfigChange(
                        createdConfig.getTenantId(),
                        CONFIG_TYPE_EMAIL_CONFIG,
                        OPERATION_INSERT,
                        null,
                        createdConfig,
                        createdConfig.getTenantId())
                    .andThen(Single.just(createdConfig)));
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
              EmailConfigModel updatedConfig = mergeEmailConfig(tenantId, requestDto, oldConfig);
              return emailConfigDao
                  .updateEmailConfig(updatedConfig)
                  .andThen(getEmailConfig(tenantId))
                  .flatMap(
                      newConfig ->
                          changelogService
                              .logConfigChange(
                                  tenantId,
                                  CONFIG_TYPE_EMAIL_CONFIG,
                                  OPERATION_UPDATE,
                                  oldConfig,
                                  newConfig,
                                  tenantId)
                              .andThen(Single.just(newConfig)));
            });
  }

  public Completable deleteEmailConfig(String tenantId) {
    return emailConfigDao
        .getEmailConfig(tenantId)
        .switchIfEmpty(Single.error(EMAIL_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldConfig ->
                emailConfigDao
                    .deleteEmailConfig(tenantId)
                    .filter(deleted -> deleted)
                    .switchIfEmpty(Single.error(EMAIL_CONFIG_NOT_FOUND.getException()))
                    .flatMapCompletable(
                        deleted ->
                            changelogService
                                .logConfigChange(
                                    tenantId,
                                    CONFIG_TYPE_EMAIL_CONFIG,
                                    OPERATION_DELETE,
                                    oldConfig,
                                    null,
                                    tenantId)
                                .andThen(Completable.complete())));
  }

  private EmailConfigModel buildEmailConfigFromCreateRequest(CreateEmailConfigRequestDto requestDto) {
    return EmailConfigModel.builder()
        .tenantId(requestDto.getTenantId())
        .isSslEnabled(requestDto.getIsSslEnabled())
        .host(requestDto.getHost())
        .port(requestDto.getPort())
        .sendEmailPath(requestDto.getSendEmailPath())
        .templateName(requestDto.getTemplateName())
        .templateParams(encodeTemplateParams(requestDto.getTemplateParams()))
        .build();
  }

  private EmailConfigModel mergeEmailConfig(
      String tenantId, UpdateEmailConfigRequestDto requestDto, EmailConfigModel oldConfig) {
    return EmailConfigModel.builder()
        .tenantId(tenantId)
        .isSslEnabled(coalesce(requestDto.getIsSslEnabled(), oldConfig.getIsSslEnabled()))
        .host(coalesce(requestDto.getHost(), oldConfig.getHost()))
        .port(coalesce(requestDto.getPort(), oldConfig.getPort()))
        .sendEmailPath(coalesce(requestDto.getSendEmailPath(), oldConfig.getSendEmailPath()))
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
