package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.dao.EmailConfigDao;
import com.dreamsportslabs.guardian.dao.model.EmailConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateEmailConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateEmailConfigRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class EmailConfigService {
  private final EmailConfigDao emailConfigDao;

  public Single<EmailConfigModel> createEmailConfig(CreateEmailConfigRequestDto requestDto) {
    EmailConfigModel emailConfigModel =
        EmailConfigModel.builder()
            .tenantId(requestDto.getTenantId())
            .isSslEnabled(
                requestDto.getIsSslEnabled() != null ? requestDto.getIsSslEnabled() : false)
            .host(requestDto.getHost())
            .port(requestDto.getPort())
            .sendEmailPath(requestDto.getSendEmailPath())
            .templateName(requestDto.getTemplateName())
            .templateParams(requestDto.getTemplateParams())
            .build();

    return emailConfigDao.createEmailConfig(emailConfigModel);
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
            existing ->
                emailConfigDao
                    .updateEmailConfig(tenantId, requestDto)
                    .andThen(getEmailConfig(tenantId)));
  }

  public Completable deleteEmailConfig(String tenantId) {
    return emailConfigDao
        .getEmailConfig(tenantId)
        .switchIfEmpty(Single.error(EMAIL_CONFIG_NOT_FOUND.getException()))
        .flatMapCompletable(
            existing ->
                emailConfigDao
                    .deleteEmailConfig(tenantId)
                    .filter(deleted -> deleted)
                    .switchIfEmpty(
                        Single.error(
                            INTERNAL_SERVER_ERROR.getCustomException(
                                "Failed to delete email config")))
                    .ignoreElement());
  }
}
