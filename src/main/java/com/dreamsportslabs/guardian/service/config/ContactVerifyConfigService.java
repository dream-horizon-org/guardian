package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.CREATE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.DELETE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.GET_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.ContactVerifyConfigQuery.UPDATE_CONTACT_VERIFY_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CONTACT_VERIFY_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CONTACT_VERIFY_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.ContactVerifyConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateContactVerifyConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateContactVerifyConfigRequestDto;
import com.dreamsportslabs.guardian.exception.ErrorEnum;
import com.dreamsportslabs.guardian.service.ChangelogService;
import com.dreamsportslabs.guardian.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContactVerifyConfigService
    extends BaseConfigService<
        ContactVerifyConfigModel,
        CreateContactVerifyConfigRequestDto,
        UpdateContactVerifyConfigRequestDto> {
  private final ObjectMapper objectMapper;

  @Inject
  public ContactVerifyConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      ObjectMapper objectMapper) {
    super(changelogService, mysqlClient, tenantCache);
    this.objectMapper = objectMapper;
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_CONTACT_VERIFY_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_CONTACT_VERIFY_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_CONTACT_VERIFY_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_CONTACT_VERIFY_CONFIG;
  }

  @Override
  protected Tuple buildParams(String tenantId, ContactVerifyConfigModel contactVerifyConfig) {
    return Tuple.tuple()
        .addValue(contactVerifyConfig.getIsOtpMocked())
        .addValue(contactVerifyConfig.getOtpLength())
        .addValue(contactVerifyConfig.getTryLimit())
        .addValue(contactVerifyConfig.getResendLimit())
        .addValue(contactVerifyConfig.getOtpResendInterval())
        .addValue(contactVerifyConfig.getOtpValidity())
        .addString(
            JsonUtils.serializeToJsonString(
                contactVerifyConfig.getWhitelistedInputs(), objectMapper))
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return CONTACT_VERIFY_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_CONTACT_VERIFY_CONFIG;
  }

  @Override
  protected Class<ContactVerifyConfigModel> getModelClass() {
    return ContactVerifyConfigModel.class;
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
