package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.BIOMETRIC_ALG_ES256;
import static com.dreamsportslabs.guardian.constant.Constants.BIOMETRIC_BINDING_TYPE_APPKEY;
import static com.dreamsportslabs.guardian.constant.Constants.TOKEN;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CHALLENGE_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CREDENTIAL_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_ENCODING;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_PUBLIC_KEY;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_SIGNATURE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_STATE;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;
import static com.dreamsportslabs.guardian.utils.Utils.getIpFromHeaders;
import static com.dreamsportslabs.guardian.utils.Utils.isValidBase64;

import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.dao.BiometricChallengeDao;
import com.dreamsportslabs.guardian.dao.CredentialsDao;
import com.dreamsportslabs.guardian.dao.model.BiometricChallengeModel;
import com.dreamsportslabs.guardian.dao.model.CredentialsModel;
import com.dreamsportslabs.guardian.dao.model.RefreshTokenModel;
import com.dreamsportslabs.guardian.dto.request.BiometricChallengeRequestDto;
import com.dreamsportslabs.guardian.dto.request.BiometricCompleteRequestDto;
import com.dreamsportslabs.guardian.dto.request.DeviceMetadataDto;
import com.dreamsportslabs.guardian.dto.request.MetaInfo;
import com.dreamsportslabs.guardian.dto.response.BiometricChallengeResponseDto;
import com.dreamsportslabs.guardian.utils.BiometricCryptoUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.MultivaluedMap;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BiometricService {
  private static final int CHALLENGE_EXPIRY_SECONDS = 300;
  private static final int CHALLENGE_SIZE_BYTES = 32;

  private final BiometricChallengeDao biometricChallengeDao;
  private final CredentialsDao credentialsDao;
  private final AuthorizationService authorizationService;
  private final UserService userService;

  public Single<BiometricChallengeResponseDto> generateChallenge(
      BiometricChallengeRequestDto requestDto, String tenantId) {

    return authorizationService
        .validateRefreshToken(tenantId, requestDto.getClientId(), requestDto.getRefreshToken())
        .flatMap(
            refreshTokenModel -> {
              byte[] challengeBytes = new byte[CHALLENGE_SIZE_BYTES];
              new java.security.SecureRandom().nextBytes(challengeBytes);
              String challenge = Base64.getEncoder().encodeToString(challengeBytes);

              String state = RandomStringUtils.randomAlphanumeric(10);

              long expiry = getCurrentTimeInSeconds() + CHALLENGE_EXPIRY_SECONDS;

              BiometricChallengeModel challengeModel =
                  BiometricChallengeModel.builder()
                      .state(state)
                      .challenge(challenge)
                      .clientId(requestDto.getClientId())
                      .userId(refreshTokenModel.getUserId())
                      .deviceMetadata(requestDto.getDeviceMetadata())
                      .refreshToken(requestDto.getRefreshToken())
                      .expiry(expiry)
                      .build();

              return biometricChallengeDao
                  .saveChallenge(challengeModel, tenantId)
                  .map(
                      model ->
                          BiometricChallengeResponseDto.builder()
                              .state(model.getState())
                              .challenge(model.getChallenge())
                              .expiresIn(CHALLENGE_EXPIRY_SECONDS)
                              .build());
            });
  }

  public Single<Object> completeBiometric(
      BiometricCompleteRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String tenantId) {

    return biometricChallengeDao
        .getChallenge(requestDto.getState(), tenantId)
        .switchIfEmpty(Single.error(CHALLENGE_NOT_FOUND.getException()))
        .flatMap(
            challengeModel -> {
              if (!challengeModel.getClientId().equals(requestDto.getClientId())) {
                return Single.error(
                    INVALID_STATE.getCustomException(
                        "State is invalid as clientId is not matching"));
              }

              if (!challengeModel.getRefreshToken().equals(requestDto.getRefreshToken())) {
                return Single.error(
                    INVALID_STATE.getCustomException(
                        "State is invalid as refresh token is not matching"));
              }

              return authorizationService
                  .validateRefreshToken(
                      tenantId, challengeModel.getClientId(), requestDto.getRefreshToken())
                  .flatMap(
                      refreshTokenModel -> {
                        boolean isRegistration = StringUtils.isNotBlank(requestDto.getPublicKey());

                        if (isRegistration) {
                          return handleRegistrationFlow(
                              requestDto, challengeModel, refreshTokenModel, headers, tenantId);
                        } else {
                          return handleLoginFlow(
                              requestDto, challengeModel, refreshTokenModel, headers, tenantId);
                        }
                      });
            });
  }

  private Single<Object> handleRegistrationFlow(
      BiometricCompleteRequestDto requestDto,
      BiometricChallengeModel challengeModel,
      RefreshTokenModel refreshTokenModel,
      MultivaluedMap<String, String> headers,
      String tenantId) {

    if (!isValidBase64(requestDto.getSignature())) {
      return Single.error(
          INVALID_ENCODING.getCustomException(
              "Invalid signature encoding. Expected Base64 DER-encoded signature."));
    }

    return verifySignature(
            requestDto.getPublicKey(), challengeModel.getChallenge(), requestDto.getSignature())
        .flatMap(
            publicKey -> {
              DeviceMetadataDto deviceMetadata = requestDto.getDeviceMetadata();
              CredentialsModel credentialModel =
                  CredentialsModel.builder()
                      .tenantId(tenantId)
                      .clientId(challengeModel.getClientId())
                      .userId(challengeModel.getUserId())
                      .deviceId(deviceMetadata != null ? deviceMetadata.getDeviceId() : null)
                      .platform(deviceMetadata != null ? deviceMetadata.getPlatform() : null)
                      .credentialId(requestDto.getCredentialId())
                      .publicKey(requestDto.getPublicKey())
                      .bindingType(BIOMETRIC_BINDING_TYPE_APPKEY)
                      .alg(BIOMETRIC_ALG_ES256)
                      .signCount(0L)
                      .aaguid(null)
                      .build();

              List<AuthMethod> combinedAuthMethods =
                  combineAuthMethods(
                      refreshTokenModel.getAuthMethod(), AuthMethod.HARDWARE_KEY_PROOF);

              MetaInfo metaInfo = createMetaInfo(requestDto.getDeviceMetadata(), headers);

              return credentialsDao
                  .upsertCredential(credentialModel)
                  .andThen(
                      generateTokensForUser(
                          refreshTokenModel, headers, tenantId, combinedAuthMethods, metaInfo))
                  .doFinally(
                      () ->
                          biometricChallengeDao
                              .deleteChallenge(requestDto.getState(), tenantId)
                              .subscribe());
            });
  }

  private Single<Object> handleLoginFlow(
      BiometricCompleteRequestDto requestDto,
      BiometricChallengeModel challengeModel,
      RefreshTokenModel refreshTokenModel,
      MultivaluedMap<String, String> headers,
      String tenantId) {

    if (!isValidBase64(requestDto.getSignature())) {
      return Single.error(
          INVALID_ENCODING.getCustomException(
              "Invalid signature encoding. Expected Base64 DER-encoded signature."));
    }

    return credentialsDao
        .getCredential(
            tenantId,
            challengeModel.getClientId(),
            challengeModel.getUserId(),
            requestDto.getCredentialId())
        .switchIfEmpty(Single.error(CREDENTIAL_NOT_FOUND.getException()))
        .flatMap(
            credentialModel ->
                verifySignature(
                        credentialModel.getPublicKey(),
                        challengeModel.getChallenge(),
                        requestDto.getSignature())
                    .flatMap(
                        publicKey -> {
                          // sign_count is always 0 for native biometric login - no DB update needed
                          List<AuthMethod> combinedAuthMethods =
                              combineAuthMethods(
                                  refreshTokenModel.getAuthMethod(), AuthMethod.HARDWARE_KEY_PROOF);

                          MetaInfo metaInfo =
                              createMetaInfo(requestDto.getDeviceMetadata(), headers);

                          return generateTokensForUser(
                                  refreshTokenModel,
                                  headers,
                                  tenantId,
                                  combinedAuthMethods,
                                  metaInfo)
                              .doFinally(
                                  () ->
                                      biometricChallengeDao
                                          .deleteChallenge(requestDto.getState(), tenantId)
                                          .subscribe());
                        }));
  }

  private Single<PublicKey> verifySignature(
      String publicKeyPem, String challengeBase64, String signatureBase64) {
    try {
      byte[] challengeBytes = Base64.getDecoder().decode(challengeBase64);

      PublicKey publicKey = BiometricCryptoUtils.convertPemPublicKeyToPublicKey(publicKeyPem);

      boolean isValid =
          BiometricCryptoUtils.verifySignature(publicKey, challengeBytes, signatureBase64);

      if (!isValid) {
        return Single.error(INVALID_SIGNATURE.getCustomException("Signature verification failed"));
      }

      return Single.just(publicKey);
    } catch (IllegalArgumentException e) {
      log.error("Failed to decode challenge", e);
      return Single.error(INVALID_ENCODING.getCustomException("Invalid challenge encoding"));
    } catch (Exception e) {
      log.error("Failed to verify signature", e);
      return Single.error(
          INVALID_PUBLIC_KEY.getCustomException("Invalid public key format: " + e.getMessage()));
    }
  }

  private List<AuthMethod> combineAuthMethods(
      List<AuthMethod> existingAuthMethods, AuthMethod newAuthMethod) {
    Set<AuthMethod> authMethodSet = new LinkedHashSet<>();

    if (existingAuthMethods != null && !existingAuthMethods.isEmpty()) {
      authMethodSet.addAll(existingAuthMethods);
    }

    authMethodSet.add(newAuthMethod);

    return new ArrayList<>(authMethodSet);
  }

  private Single<Object> generateTokensForUser(
      RefreshTokenModel refreshTokenModel,
      MultivaluedMap<String, String> headers,
      String tenantId,
      List<AuthMethod> authMethods,
      MetaInfo metaInfo) {

    return userService
        .getUser(Map.of(USERID, refreshTokenModel.getUserId()), headers, tenantId)
        .flatMap(
            userResponse -> {
              String scopes = String.join(" ", refreshTokenModel.getScope());
              return authorizationService.generate(
                  userResponse,
                  TOKEN,
                  scopes,
                  authMethods,
                  metaInfo,
                  refreshTokenModel.getClientId(),
                  tenantId);
            });
  }

  private MetaInfo createMetaInfo(
      DeviceMetadataDto deviceMetadata, MultivaluedMap<String, String> headers) {
    MetaInfo metaInfo = new MetaInfo();

    if (deviceMetadata != null && StringUtils.isNotBlank(deviceMetadata.getDeviceName())) {
      metaInfo.setDeviceName(deviceMetadata.getDeviceName());
    }

    metaInfo.setIp(getIpFromHeaders(headers));

    return metaInfo;
  }
}
