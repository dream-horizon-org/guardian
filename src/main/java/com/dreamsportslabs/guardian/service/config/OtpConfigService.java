package com.dreamsportslabs.guardian.service.config;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DUPLICATE_ENTRY_MESSAGE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.CREATE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.DELETE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.GET_OTP_CONFIG;
import static com.dreamsportslabs.guardian.dao.config.query.OtpConfigQuery.UPDATE_OTP_CONFIG;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OTP_CONFIG_ALREADY_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OTP_CONFIG_NOT_FOUND;
import static com.dreamsportslabs.guardian.utils.Utils.coalesce;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.client.MysqlClient;
import com.dreamsportslabs.guardian.dao.model.config.OtpConfigModel;
import com.dreamsportslabs.guardian.dto.request.config.CreateOtpConfigRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateOtpConfigRequestDto;
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
public class OtpConfigService
    extends BaseConfigService<
        OtpConfigModel, CreateOtpConfigRequestDto, UpdateOtpConfigRequestDto> {
  private final ObjectMapper objectMapper;

  @Inject
  public OtpConfigService(
      ChangelogService changelogService,
      MysqlClient mysqlClient,
      TenantCache tenantCache,
      ObjectMapper objectMapper) {
    super(changelogService, mysqlClient, tenantCache);
    this.objectMapper = objectMapper;
  }

  @Override
  protected String getCreateQuery() {
    return CREATE_OTP_CONFIG;
  }

  @Override
  protected String getGetQuery() {
    return GET_OTP_CONFIG;
  }

  @Override
  protected String getUpdateQuery() {
    return UPDATE_OTP_CONFIG;
  }

  @Override
  protected String getDeleteQuery() {
    return DELETE_OTP_CONFIG;
  }

  @Override
  protected Tuple buildParams(String tenantId, OtpConfigModel otpConfig) {
    return Tuple.tuple()
        .addValue(otpConfig.getIsOtpMocked())
        .addInteger(otpConfig.getOtpLength())
        .addInteger(otpConfig.getTryLimit())
        .addInteger(otpConfig.getResendLimit())
        .addInteger(otpConfig.getOtpResendInterval())
        .addInteger(otpConfig.getOtpValidity())
        .addInteger(otpConfig.getOtpSendWindowSeconds())
        .addInteger(otpConfig.getOtpSendWindowMaxCount())
        .addInteger(otpConfig.getOtpSendBlockSeconds())
        .addString(JsonUtils.serializeToJsonString(otpConfig.getWhitelistedInputs(), objectMapper))
        .addString(tenantId);
  }

  @Override
  protected ErrorEnum getDuplicateEntryError() {
    return OTP_CONFIG_ALREADY_EXISTS;
  }

  @Override
  protected String getDuplicateEntryMessageFormat() {
    return DUPLICATE_ENTRY_MESSAGE_OTP_CONFIG;
  }

  @Override
  protected Class<OtpConfigModel> getModelClass() {
    return OtpConfigModel.class;
  }

  @Override
  protected String getConfigType() {
    return CONFIG_TYPE_OTP_CONFIG;
  }

  @Override
  protected ErrorEnum getNotFoundError() {
    return OTP_CONFIG_NOT_FOUND;
  }

  @Override
  protected String getCreateErrorMessage() {
    return "Failed to create OTP config";
  }

  @Override
  protected String getUpdateErrorMessage() {
    return "Failed to update OTP config";
  }

  @Override
  protected OtpConfigModel mapToModel(CreateOtpConfigRequestDto requestDto) {
    return OtpConfigModel.builder()
        .isOtpMocked(requestDto.getIsOtpMocked())
        .otpLength(requestDto.getOtpLength())
        .tryLimit(requestDto.getTryLimit())
        .resendLimit(requestDto.getResendLimit())
        .otpResendInterval(requestDto.getOtpResendInterval())
        .otpValidity(requestDto.getOtpValidity())
        .otpSendWindowSeconds(requestDto.getOtpSendWindowSeconds())
        .otpSendWindowMaxCount(requestDto.getOtpSendWindowMaxCount())
        .otpSendBlockSeconds(requestDto.getOtpSendBlockSeconds())
        .whitelistedInputs(requestDto.getWhitelistedInputs())
        .build();
  }

  @Override
  protected OtpConfigModel mergeModel(
      UpdateOtpConfigRequestDto requestDto, OtpConfigModel oldConfig) {
    return OtpConfigModel.builder()
        .isOtpMocked(coalesce(requestDto.getIsOtpMocked(), oldConfig.getIsOtpMocked()))
        .otpLength(coalesce(requestDto.getOtpLength(), oldConfig.getOtpLength()))
        .tryLimit(coalesce(requestDto.getTryLimit(), oldConfig.getTryLimit()))
        .resendLimit(coalesce(requestDto.getResendLimit(), oldConfig.getResendLimit()))
        .otpResendInterval(
            coalesce(requestDto.getOtpResendInterval(), oldConfig.getOtpResendInterval()))
        .otpValidity(coalesce(requestDto.getOtpValidity(), oldConfig.getOtpValidity()))
        .otpSendWindowSeconds(
            coalesce(requestDto.getOtpSendWindowSeconds(), oldConfig.getOtpSendWindowSeconds()))
        .otpSendWindowMaxCount(
            coalesce(requestDto.getOtpSendWindowMaxCount(), oldConfig.getOtpSendWindowMaxCount()))
        .otpSendBlockSeconds(
            coalesce(requestDto.getOtpSendBlockSeconds(), oldConfig.getOtpSendBlockSeconds()))
        .whitelistedInputs(
            coalesce(requestDto.getWhitelistedInputs(), oldConfig.getWhitelistedInputs()))
        .build();
  }

  public Single<OtpConfigModel> getOtpConfig(String tenantId) {
    return getConfig(tenantId);
  }

  public Single<OtpConfigModel> updateOtpConfig(
      String tenantId, UpdateOtpConfigRequestDto requestDto, String userIdentifier) {
    return updateConfig(tenantId, requestDto, userIdentifier);
  }

  public Completable deleteOtpConfig(String tenantId, String userIdentifier) {
    return deleteConfig(tenantId, userIdentifier);
  }
}
