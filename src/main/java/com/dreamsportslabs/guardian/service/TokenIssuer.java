package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_AMR;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_AUD;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_CLIENT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_EXP;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_IAT;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_ISS;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_JTI;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_NONCE;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_RFT_ID;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SCOPE;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_CLAIMS_SUB;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_HEADERS_KID;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_HEADERS_TYP;
import static com.dreamsportslabs.guardian.constant.Constants.JWT_TENANT_ID_CLAIM;
import static com.dreamsportslabs.guardian.constant.Constants.TYP_JWT_ACCESS_TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.utils.Utils.getRftId;
import static com.dreamsportslabs.guardian.utils.Utils.shouldSetAccessTokenAdditionalClaims;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.fusionauth.jwt.JWTEncoder;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSASigner;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TokenIssuer {
  private final Vertx vertx;
  private final JWTEncoder encoder = JWT.getEncoder();
  private final Registry registry;

  public Single<String> generateIdToken(
      long iat,
      String nonce,
      JsonObject user,
      List<String> idTokenClaims,
      String clientId,
      String tenantId) {
    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    JWT jwt = new JWT();

    jwt.addClaim(JWT_CLAIMS_AUD, clientId);
    jwt.addClaim(JWT_CLAIMS_EXP, iat + tenantConfig.getTokenConfig().getIdTokenExpiry());
    jwt.addClaim(JWT_CLAIMS_SUB, user.getString(USERID));
    jwt.addClaim(JWT_CLAIMS_IAT, iat);
    jwt.addClaim(JWT_CLAIMS_ISS, tenantConfig.getTokenConfig().getIssuer());
    if (nonce != null) {
      jwt.addClaim(JWT_CLAIMS_NONCE, nonce);
    }
    for (String claim : idTokenClaims) {
      Object value = user.getValue(claim);
      if (value != null) {
        jwt.addClaim(claim, value);
      }
    }
    return signToken(jwt, tenantId);
  }

  public Single<String> generateAccessToken(
      String refreshToken,
      long iat,
      String scope,
      JsonObject userResponse,
      List<AuthMethod> authMethods,
      String clientId,
      String tenantId,
      TenantConfig config) {
    JWT jwt = new JWT();
    jwt.addClaim(JWT_TENANT_ID_CLAIM, tenantId);
    jwt.addClaim(JWT_CLAIMS_AUD, clientId);
    jwt.addClaim(JWT_CLAIMS_SUB, userResponse.getString(USERID));
    jwt.addClaim(JWT_CLAIMS_IAT, iat);
    jwt.addClaim(JWT_CLAIMS_ISS, config.getTokenConfig().getIssuer());
    jwt.addClaim(JWT_CLAIMS_RFT_ID, getRftId(refreshToken));
    jwt.addClaim(JWT_CLAIMS_EXP, iat + config.getTokenConfig().getAccessTokenExpiry());
    jwt.addClaim(JWT_CLAIMS_CLIENT_ID, clientId);
    jwt.addClaim(JWT_CLAIMS_JTI, RandomStringUtils.randomAlphanumeric(32));
    jwt.addClaim(JWT_CLAIMS_SCOPE, scope);
    jwt.addClaim(JWT_CLAIMS_AMR, authMethods.stream().map(AuthMethod::getValue).toList());
    if (shouldSetAccessTokenAdditionalClaims(config)) {
      addAdditionalClaimsFromJsonPath(
          jwt, userResponse, config.getTokenConfig().getAccessTokenClaims());
    }

    Map<String, String> tokenHeaders = new HashMap<>();
    tokenHeaders.put(JWT_HEADERS_TYP, TYP_JWT_ACCESS_TOKEN);
    return signToken(jwt, tenantId, tokenHeaders);
  }

  public String generateRefreshToken() {
    return RandomStringUtils.randomAlphanumeric(32);
  }

  public String generateSsoToken() {
    return RandomStringUtils.randomAlphanumeric(15);
  }

  private Single<String> signToken(JWT jwt, String tenantId) {
    return signToken(jwt, tenantId, new HashMap<>());
  }

  private Single<String> signToken(JWT jwt, String tenantId, Map<String, String> headers) {
    return vertx
        .rxExecuteBlocking(
            future -> {
              RSASigner signer = registry.get(tenantId, RSASigner.class);
              future.complete(
                  encoder.encode(
                      jwt,
                      signer,
                      header -> {
                        for (Map.Entry<String, String> entry : headers.entrySet()) {
                          header.set(entry.getKey(), entry.getValue());
                        }
                        header.set(JWT_HEADERS_KID, signer.getKid());
                      }));
            },
            false)
        .switchIfEmpty(Single.error(INTERNAL_SERVER_ERROR.getException()))
        .onErrorResumeNext(err -> Single.error(INTERNAL_SERVER_ERROR.getException(err)))
        .map(String.class::cast);
  }

  private void addAdditionalClaimsFromJsonPath(
      JWT jwt, JsonObject userResponse, List<String> claimPaths) {
    if (claimPaths == null || claimPaths.isEmpty()) return;

    DocumentContext jsonContext = JsonPath.parse(userResponse.encode());

    for (String claimPath : claimPaths) {
      String path = claimPath != null ? claimPath.trim() : "";
      if (path.isEmpty()) continue;

      try {
        String jsonPathExpr = path.startsWith("$") ? path : "$." + path;
        Object value = jsonContext.read(jsonPathExpr);

        if (value instanceof List<?> list && !list.isEmpty()) {
          value = list.get(0);
        }

        if (value != null) {
          String claimName = extractClaimName(path);
          jwt.addClaim(claimName, value);
        }
      } catch (PathNotFoundException e) {
        log.debug("Path not found: {}", path);
      } catch (Exception e) {
        log.warn("Error reading path '{}': {}", path, e.getMessage());
      }
    }
  }

  private String extractClaimName(String path) {
    String cleanPath =
        path.startsWith("$.") ? path.substring(2) : path.startsWith("$") ? path.substring(1) : path;

    int lastDot = cleanPath.lastIndexOf('.');
    String claimName = lastDot >= 0 ? cleanPath.substring(lastDot + 1) : cleanPath;

    int bracketIndex = claimName.indexOf('[');
    return bracketIndex > 0 ? claimName.substring(0, bracketIndex) : claimName;
  }
}
