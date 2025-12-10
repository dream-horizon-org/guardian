package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_TENANT;
import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_TOKEN_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.CONFIG_TYPE_USER_CONFIG;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_ID_TOKEN_CLAIM_EMAIL_ID;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_ID_TOKEN_CLAIM_USER_ID;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_RSA_KEY_COUNT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_RSA_KEY_SIZE;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_ACCESS_TOKEN_EXPIRY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_ALGORITHM;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_COOKIE_DOMAIN;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_COOKIE_HTTP_ONLY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_COOKIE_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_COOKIE_SAME_SITE;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_COOKIE_SECURE;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_ID_TOKEN_EXPIRY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_ISSUER;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_TOKEN_CONFIG_REFRESH_TOKEN_EXPIRY;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_ADD_PROVIDER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_AUTHENTICATE_USER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_CREATE_USER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_GET_USER_PATH;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_HOST;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_IS_SSL_ENABLED;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_PORT;
import static com.dreamsportslabs.guardian.constant.Constants.DEFAULT_USER_CONFIG_SEND_PROVIDER_DETAILS;
import static com.dreamsportslabs.guardian.constant.Constants.FIRST_RSA_KEY_INDEX;
import static com.dreamsportslabs.guardian.constant.Constants.FORMAT_PEM;
import static com.dreamsportslabs.guardian.constant.Constants.JSON_FIELD_CURRENT;
import static com.dreamsportslabs.guardian.constant.Constants.JSON_FIELD_KID;
import static com.dreamsportslabs.guardian.constant.Constants.JSON_FIELD_PRIVATE_KEY;
import static com.dreamsportslabs.guardian.constant.Constants.JSON_FIELD_PUBLIC_KEY;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_INSERT;
import static com.dreamsportslabs.guardian.constant.Constants.OPERATION_UPDATE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_NOT_FOUND;

import com.dreamsportslabs.guardian.dao.ChangelogDao;
import com.dreamsportslabs.guardian.dao.TenantDao;
import com.dreamsportslabs.guardian.dao.model.TenantModel;
import com.dreamsportslabs.guardian.dao.model.TokenConfigModel;
import com.dreamsportslabs.guardian.dao.model.UserConfigModel;
import com.dreamsportslabs.guardian.dto.request.GenerateRsaKeyRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.CreateTenantRequestDto;
import com.dreamsportslabs.guardian.dto.request.config.UpdateTenantRequestDto;
import com.dreamsportslabs.guardian.dto.response.RsaKeyResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TenantService {
  private static final String EMPTY_JSON_ARRAY = new JsonArray().encode();
  private static final JsonArray DEFAULT_ID_TOKEN_CLAIMS_ARRAY =
      new JsonArray().add(DEFAULT_ID_TOKEN_CLAIM_USER_ID).add(DEFAULT_ID_TOKEN_CLAIM_EMAIL_ID);

  private final TenantDao tenantDao;
  private final ChangelogDao changelogDao;
  private final RsaKeyPairGeneratorService rsaKeyPairGeneratorService;

  public Single<TenantModel> createTenant(CreateTenantRequestDto requestDto) {
    TenantModel tenantModel =
        TenantModel.builder().id(requestDto.getId()).name(requestDto.getName()).build();

    return tenantDao
        .createTenant(tenantModel)
        .flatMap(
            createdTenant ->
                logTenantChange(createdTenant.getId(), OPERATION_INSERT, null, createdTenant)
                    .andThen(createMandatoryConfigs(createdTenant.getId()))
                    .andThen(Single.just(createdTenant)));
  }

  private Completable createMandatoryConfigs(String tenantId) {
    return createDefaultUserConfig(tenantId).andThen(createDefaultTokenConfig(tenantId));
  }

  private Completable createDefaultUserConfig(String tenantId) {
    UserConfigModel userConfig = buildDefaultUserConfig(tenantId);
    return tenantDao
        .createDefaultUserConfig(userConfig)
        .andThen(logConfigCreation(tenantId, CONFIG_TYPE_USER_CONFIG, userConfig));
  }

  private UserConfigModel buildDefaultUserConfig(String tenantId) {
    return UserConfigModel.builder()
        .tenantId(tenantId)
        .isSslEnabled(DEFAULT_USER_CONFIG_IS_SSL_ENABLED)
        .host(DEFAULT_USER_CONFIG_HOST)
        .port(DEFAULT_USER_CONFIG_PORT)
        .getUserPath(DEFAULT_USER_CONFIG_GET_USER_PATH)
        .createUserPath(DEFAULT_USER_CONFIG_CREATE_USER_PATH)
        .authenticateUserPath(DEFAULT_USER_CONFIG_AUTHENTICATE_USER_PATH)
        .addProviderPath(DEFAULT_USER_CONFIG_ADD_PROVIDER_PATH)
        .sendProviderDetails(DEFAULT_USER_CONFIG_SEND_PROVIDER_DETAILS)
        .build();
  }

  private Completable createDefaultTokenConfig(String tenantId) {
    TokenConfigModel tokenConfig = buildDefaultTokenConfig(tenantId);
    return tenantDao
        .createDefaultTokenConfig(tokenConfig)
        .andThen(logConfigCreation(tenantId, CONFIG_TYPE_TOKEN_CONFIG, tokenConfig));
  }

  private TokenConfigModel buildDefaultTokenConfig(String tenantId) {
    return TokenConfigModel.builder()
        .tenantId(tenantId)
        .algorithm(DEFAULT_TOKEN_CONFIG_ALGORITHM)
        .issuer(DEFAULT_TOKEN_CONFIG_ISSUER)
        .rsaKeys(generateRsaKeysArray().encode())
        .accessTokenExpiry(DEFAULT_TOKEN_CONFIG_ACCESS_TOKEN_EXPIRY)
        .refreshTokenExpiry(DEFAULT_TOKEN_CONFIG_REFRESH_TOKEN_EXPIRY)
        .idTokenExpiry(DEFAULT_TOKEN_CONFIG_ID_TOKEN_EXPIRY)
        .idTokenClaims(buildDefaultIdTokenClaims().encode())
        .cookieSameSite(DEFAULT_TOKEN_CONFIG_COOKIE_SAME_SITE)
        .cookieDomain(DEFAULT_TOKEN_CONFIG_COOKIE_DOMAIN)
        .cookiePath(DEFAULT_TOKEN_CONFIG_COOKIE_PATH)
        .cookieSecure(DEFAULT_TOKEN_CONFIG_COOKIE_SECURE)
        .cookieHttpOnly(DEFAULT_TOKEN_CONFIG_COOKIE_HTTP_ONLY)
        .accessTokenClaims(EMPTY_JSON_ARRAY)
        .build();
  }

  private JsonArray generateRsaKeysArray() {
    GenerateRsaKeyRequestDto keyRequest = buildRsaKeyRequest();
    JsonArray rsaKeysArray = new JsonArray();

    for (int i = 0; i < DEFAULT_RSA_KEY_COUNT; i++) {
      RsaKeyResponseDto rsaKey = rsaKeyPairGeneratorService.generateKey(keyRequest);
      JsonObject rsaKeyObject = buildRsaKeyObject(rsaKey, i == FIRST_RSA_KEY_INDEX);
      rsaKeysArray.add(rsaKeyObject);
    }

    return rsaKeysArray;
  }

  private GenerateRsaKeyRequestDto buildRsaKeyRequest() {
    GenerateRsaKeyRequestDto keyRequest = new GenerateRsaKeyRequestDto();
    keyRequest.setKeySize(DEFAULT_RSA_KEY_SIZE);
    keyRequest.setFormat(FORMAT_PEM);
    return keyRequest;
  }

  private JsonObject buildRsaKeyObject(RsaKeyResponseDto rsaKey, boolean isCurrent) {
    JsonObject rsaKeyObject = new JsonObject();
    rsaKeyObject.put(JSON_FIELD_KID, rsaKey.getKid());
    rsaKeyObject.put(JSON_FIELD_PUBLIC_KEY, rsaKey.getPublicKey().toString());
    rsaKeyObject.put(JSON_FIELD_PRIVATE_KEY, rsaKey.getPrivateKey().toString());
    if (isCurrent) {
      rsaKeyObject.put(JSON_FIELD_CURRENT, true);
    }
    return rsaKeyObject;
  }

  private JsonArray buildDefaultIdTokenClaims() {
    return DEFAULT_ID_TOKEN_CLAIMS_ARRAY.copy();
  }

  private Completable logConfigCreation(String tenantId, String configType, Object config) {
    JsonObject configJson = JsonObject.mapFrom(config);
    return changelogDao.logConfigChange(
        tenantId, configType, OPERATION_INSERT, null, configJson, tenantId);
  }

  public Single<TenantModel> getTenant(String tenantId) {
    return tenantDao
        .getTenant(tenantId)
        .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()));
  }

  public Single<TenantModel> getTenantByName(String name) {
    return tenantDao
        .getTenantByName(name)
        .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()));
  }

  public Single<TenantModel> updateTenant(String tenantId, UpdateTenantRequestDto requestDto) {
    return tenantDao
        .getTenant(tenantId)
        .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()))
        .flatMap(
            oldTenant -> {
              JsonObject oldValues = JsonObject.mapFrom(oldTenant);
              return tenantDao
                  .updateTenant(tenantId, requestDto.getName())
                  .andThen(getTenant(tenantId))
                  .flatMap(
                      newTenant ->
                          logTenantChange(tenantId, OPERATION_UPDATE, oldValues, newTenant)
                              .andThen(Single.just(newTenant)));
            });
  }

  private Completable logTenantChange(
      String tenantId, String operation, JsonObject oldValues, TenantModel newTenant) {
    return changelogDao.logConfigChange(
        tenantId,
        CONFIG_TYPE_TENANT,
        operation,
        oldValues,
        JsonObject.mapFrom(newTenant),
        tenantId);
  }

  public Completable deleteTenant(String tenantId) {
    return tenantDao
        .getTenant(tenantId)
        .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()))
        .flatMapCompletable(
            oldTenant ->
                tenantDao
                    .deleteTenant(tenantId)
                    .filter(deleted -> deleted)
                    .switchIfEmpty(Single.error(TENANT_NOT_FOUND.getException()))
                    .ignoreElement());
  }
}
