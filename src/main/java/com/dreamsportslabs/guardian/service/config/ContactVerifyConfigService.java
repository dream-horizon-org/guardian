package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CONTACT_VERIFY_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.config.BaseConfigDao;
import com.dreamsportslabs.guardian.dao.config.ContactVerifyConfigDao;
import com.dreamsportslabs.guardian.dao.model.config.ContactVerifyConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateContactVerifyConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateContactVerifyConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContactVerifyConfigService
    extends BaseConfigService<
        ContactVerifyConfigModel,
        CreateContactVerifyConfigRequestDto,
        UpdateContactVerifyConfigRequestDto> {
  private final ContactVerifyConfigDao contactVerifyConfigDao;

  @Inject
  public ContactVerifyConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      ContactVerifyConfigDao contactVerifyConfigDao) {
    super(changelogService, mysqlClient, tenantCache);
    this.contactVerifyConfigDao = contactVerifyConfigDao;
  }

  @Override
  protected BaseConfigDao<ContactVerifyConfigModel> getDao() {
    return contactVerifyConfigDao;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_CONTACT_VERIFY_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return CONTACT_VERIFY_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create contact verify config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update contact verify config";
  }

  @Override
  protected ContactVerifyConfigModel mapToModel(CreateContactVerifyConfigRequestDto requestDto) {
    return ContactVerifyConfigModel.builder()
        .isOtpMocked(requestDto.getIsOtpMocked())
        .otpLength(requestDto.getOtpLength())
        .tryLimit(requestDto.getTryLimit())
        .resendLimit(requestDto.getResendLimit())
        .otpResendInterval(requestDto.getOtpResendInterval())
        .otpValidity(requestDto.getOtpValidity())
        .whitelistedInputs(requestDto.getWhitelistedInputs())
        .build();
  }

  @Override
  protected ContactVerifyConfigModel mergeModel(
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

  public Single<ContactVerifyConfigModel> createContactVerifyConfig(
      String tenantId, CreateContactVerifyConfigRequestDto requestDto) {
    return createConfig(tenantId, requestDto);
  }

  public Single<ContactVerifyConfigModel> getContactVerifyConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<ContactVerifyConfigModel> updateContactVerifyConfig(
      String tenantId, UpdateContactVerifyConfigRequestDto requestDto) {
    return updateConfig(tenantId, requestDto);
  }

  public Completable deleteContactVerifyConfig(String tenantId) {
    return deleteConfig(tenantId);
  }
}
