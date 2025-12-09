package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.FORMAT_PEM;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.TENANT_NOT_FOUND;

import com.dreamsportslabs.guardian.dao.ChangelogDao;
import com.dreamsportslabs.guardian.dao.TenantDao;
import com.dreamsportslabs.guardian.dao.model.TenantModel;
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
                changelogDao
                    .logConfigChange(
                        createdTenant.getId(),
                        "tenant",
                        "INSERT",
                        null,
                        JsonObject.mapFrom(createdTenant),
                        createdTenant.getId())
                    .andThen(createMandatoryConfigs(createdTenant.getId()))
                    .andThen(Single.just(createdTenant)));
  }

  private Completable createMandatoryConfigs(String tenantId) {
    return createDefaultUserConfig(tenantId).andThen(createDefaultTokenConfig(tenantId));
  }

  private Completable createDefaultUserConfig(String tenantId) {
    JsonObject userConfig = new JsonObject();
    userConfig.put("tenant_id", tenantId);
    userConfig.put("is_ssl_enabled", false);
    userConfig.put("host", "control-tower.dream11.local");
    userConfig.put("port", 80);
    userConfig.put("get_user_path", "/users/validate");
    userConfig.put("create_user_path", "/users");
    userConfig.put("authenticate_user_path", "/api/user/validate");
    userConfig.put("add_provider_path", "");
    userConfig.put("send_provider_details", false);

    return tenantDao
        .createDefaultUserConfig(userConfig)
        .andThen(
            changelogDao.logConfigChange(
                tenantId, "user_config", "INSERT", null, userConfig, tenantId));
  }

  private Completable createDefaultTokenConfig(String tenantId) {
    GenerateRsaKeyRequestDto keyRequest = new GenerateRsaKeyRequestDto();
    keyRequest.setKeySize(2048);
    keyRequest.setFormat(FORMAT_PEM);

    RsaKeyResponseDto rsaKey = rsaKeyPairGeneratorService.generateKey(keyRequest);

    JsonArray rsaKeysArray = new JsonArray();
    JsonObject rsaKeyObject = new JsonObject();
    rsaKeyObject.put("kid", rsaKey.getKid());
    rsaKeyObject.put("public_key", rsaKey.getPublicKey().toString());
    rsaKeyObject.put("private_key", rsaKey.getPrivateKey().toString());
    rsaKeyObject.put("current", true);
    rsaKeysArray.add(rsaKeyObject);

    JsonObject tokenConfig = new JsonObject();
    tokenConfig.put("tenant_id", tenantId);
    tokenConfig.put("algorithm", "RS512");
    tokenConfig.put("issuer", "https://dream11.local");
    tokenConfig.put("rsa_keys", rsaKeysArray);
    tokenConfig.put("access_token_expiry", 900);
    tokenConfig.put("refresh_token_expiry", 2592000);
    tokenConfig.put("id_token_expiry", 36000);
    tokenConfig.put("id_token_claims", new JsonArray().add("userId").add("emailId"));
    tokenConfig.put("cookie_same_site", "NONE");
    tokenConfig.put("cookie_domain", "");
    tokenConfig.put("cookie_path", "/");
    tokenConfig.put("cookie_secure", false);
    tokenConfig.put("cookie_http_only", true);
    tokenConfig.put("access_token_claims", new JsonArray());

    return tenantDao
        .createDefaultTokenConfig(tokenConfig)
        .andThen(
            changelogDao.logConfigChange(
                tenantId, "token_config", "INSERT", null, tokenConfig, tenantId));
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
                          changelogDao
                              .logConfigChange(
                                  tenantId,
                                  "tenant",
                                  "UPDATE",
                                  oldValues,
                                  JsonObject.mapFrom(newTenant),
                                  tenantId)
                              .andThen(Single.just(newTenant)));
            });
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
