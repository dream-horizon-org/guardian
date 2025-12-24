package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.BIOMETRIC_ALG_ES256;
import static com.dreamsportslabs.guardian.constant.Constants.BIOMETRIC_BINDING_TYPE_APPKEY;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CHALLENGE_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CREDENTIAL_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_ENCODING;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_PUBLIC_KEY;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_SIGNATURE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_STATE;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;
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
import com.dreamsportslabs.guardian.dto.response.BiometricChallengeResponseDto;
import com.dreamsportslabs.guardian.dto.response.BiometricTokenResponseDto;
import com.dreamsportslabs.guardian.utils.BiometricCryptoUtils;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
              byte[] challengeBytes =
                  java.security.SecureRandom.getInstanceStrong().generateSeed(CHALLENGE_SIZE_BYTES);

              byte[] stateBytes = java.security.SecureRandom.getInstanceStrong().generateSeed(16);

              BiometricChallengeModel challengeModel =
                  BiometricChallengeModel.builder()
                      .state(Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes))
                      .challenge(Base64.getEncoder().encodeToString(challengeBytes))
                      .clientId(requestDto.getClientId())
                      .userId(refreshTokenModel.getUserId())
                      .deviceMetadata(requestDto.getDeviceMetadata())
                      .refreshToken(requestDto.getRefreshToken())
                      .expiry(getCurrentTimeInSeconds() + CHALLENGE_EXPIRY_SECONDS)
                      .build();

              String deviceId = requestDto.getDeviceMetadata().getDeviceId();

              return Single.zip(
                  biometricChallengeDao.saveChallenge(challengeModel, tenantId),
                  getCredentialId(
                      tenantId, requestDto.getClientId(), refreshTokenModel.getUserId(), deviceId),
                  (model, credentialId) ->
                      BiometricChallengeResponseDto.builder()
                          .state(model.getState())
                          .challenge(model.getChallenge())
                          .expiresIn(CHALLENGE_EXPIRY_SECONDS)
                          .credentialId(credentialId.isEmpty() ? null : credentialId)
                          .build());
            });
  }

  public Single<BiometricTokenResponseDto> completeBiometric(
      BiometricCompleteRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String tenantId) {

    return biometricChallengeDao
        .getChallenge(requestDto.getState(), tenantId)
        .switchIfEmpty(Single.error(CHALLENGE_NOT_FOUND.getException()))
        .flatMap(challengeModel -> validateChallenge(challengeModel, requestDto))
        .flatMap(
            validatedChallenge ->
                authorizationService
                    .validateRefreshToken(
                        tenantId, validatedChallenge.getClientId(), requestDto.getRefreshToken())
                    .flatMap(
                        refreshTokenModel -> {
                          final boolean isRegistrationFlow =
                              requestDto.getPublicKey() != null
                                  && !requestDto.getPublicKey().isBlank();

                          return isRegistrationFlow
                              ? handleRegistrationFlow(
                                  requestDto,
                                  validatedChallenge,
                                  refreshTokenModel,
                                  headers,
                                  tenantId)
                              : handleLoginFlow(
                                  requestDto,
                                  validatedChallenge,
                                  refreshTokenModel,
                                  headers,
                                  tenantId);
                        }));
  }

  private Single<BiometricTokenResponseDto> handleRegistrationFlow(
      BiometricCompleteRequestDto requestDto,
      BiometricChallengeModel challengeModel,
      RefreshTokenModel refreshTokenModel,
      MultivaluedMap<String, String> headers,
      String tenantId) {

    DeviceMetadataDto deviceMetadata = requestDto.getDeviceMetadata();
    CredentialsModel credentialModel =
        CredentialsModel.builder()
            .tenantId(tenantId)
            .clientId(challengeModel.getClientId())
            .userId(challengeModel.getUserId())
            .deviceId(deviceMetadata.getDeviceId())
            .platform(deviceMetadata.getPlatform())
            .credentialId(requestDto.getCredentialId())
            .publicKey(requestDto.getPublicKey())
            .bindingType(BIOMETRIC_BINDING_TYPE_APPKEY)
            .alg(BIOMETRIC_ALG_ES256)
            .signCount(0L)
            .aaguid(null)
            .build();

    return Single.fromCallable(
            () -> {
              verifySignature(
                  requestDto.getPublicKey(),
                  challengeModel.getChallenge(),
                  requestDto.getSignature());
              return credentialModel;
            })
        .onErrorResumeNext(
            err -> {
              if (err instanceof WebApplicationException) {
                return Single.error(err);
              }
              return Single.error(INTERNAL_SERVER_ERROR.getException(err));
            })
        .flatMapCompletable(credentialsDao::insertCredential)
        .andThen(
            generateTokensForUser(
                refreshTokenModel,
                headers,
                tenantId,
                combineAuthMethods(
                    refreshTokenModel.getAuthMethod(), AuthMethod.HARDWARE_KEY_PROOF)))
        .doFinally(() -> biometricChallengeDao.deleteChallenge(requestDto.getState(), tenantId));
  }

  private Single<BiometricTokenResponseDto> handleLoginFlow(
      BiometricCompleteRequestDto requestDto,
      BiometricChallengeModel challengeModel,
      RefreshTokenModel refreshTokenModel,
      MultivaluedMap<String, String> headers,
      String tenantId) {

    return credentialsDao
        .getCredential(
            tenantId,
            challengeModel.getClientId(),
            challengeModel.getUserId(),
            requestDto.getDeviceMetadata().getDeviceId())
        .switchIfEmpty(Single.error(CREDENTIAL_NOT_FOUND.getException()))
        .flatMap(
            credentialModel -> {
              try {
                verifySignature(
                    credentialModel.getPublicKey(),
                    challengeModel.getChallenge(),
                    requestDto.getSignature());
              } catch (WebApplicationException e) {
                return Single.error(e);
              }

              List<AuthMethod> combinedAuthMethods =
                  combineAuthMethods(
                      refreshTokenModel.getAuthMethod(), AuthMethod.HARDWARE_KEY_PROOF);

              return generateTokensForUser(
                      refreshTokenModel, headers, tenantId, combinedAuthMethods)
                  .doFinally(
                      () -> biometricChallengeDao.deleteChallenge(requestDto.getState(), tenantId));
            });
  }

  private Single<BiometricChallengeModel> validateChallenge(
      BiometricChallengeModel challengeModel, BiometricCompleteRequestDto requestDto) {
    // Validate signature encoding (common for both registration and login flows)
    if (!isValidBase64(requestDto.getSignature())) {
      return Single.error(
          INVALID_ENCODING.getCustomException(
              "Invalid signature encoding. Expected Base64 DER-encoded signature."));
    }

    // Validate challenge has not expired (defense in depth - Redis TTL is primary)
    if (challengeModel.getExpiry() != null
        && challengeModel.getExpiry() <= getCurrentTimeInSeconds()) {
      return Single.error(CHALLENGE_NOT_FOUND.getCustomException("Challenge has expired"));
    }

    if (!challengeModel.getClientId().equals(requestDto.getClientId())) {
      return Single.error(
          INVALID_STATE.getCustomException("State is invalid as clientId is not matching"));
    }

    if (!challengeModel.getRefreshToken().equals(requestDto.getRefreshToken())) {
      return Single.error(
          INVALID_STATE.getCustomException("State is invalid as refresh token is not matching"));
    }

    return Single.just(challengeModel);
  }

  private Single<String> getCredentialId(
      String tenantId, String clientId, String userId, String deviceId) {
    return credentialsDao
        .getCredential(tenantId, clientId, userId, deviceId)
        .map(CredentialsModel::getCredentialId)
        .defaultIfEmpty("");
  }

  private void verifySignature(
      String publicKeyPem, String challengeBase64, String signatureBase64) {
    try {
      byte[] challengeBytes = Base64.getDecoder().decode(challengeBase64);

      PublicKey publicKey = BiometricCryptoUtils.convertPemPublicKeyToPublicKey(publicKeyPem);

      boolean isValid =
          BiometricCryptoUtils.verifySignature(publicKey, challengeBytes, signatureBase64);

      if (!isValid) {
        throw INVALID_SIGNATURE.getCustomException("Signature verification failed");
      }
    } catch (IllegalArgumentException e) {
      log.error("Failed to decode challenge", e);
      throw INVALID_ENCODING.getCustomException("Invalid challenge encoding");
    } catch (WebApplicationException e) {
      log.error("Signature verification failed with WebApplicationException", e);
      throw e;
    } catch (Exception e) {
      log.error("Failed to verify signature", e);
      throw INVALID_PUBLIC_KEY.getCustomException("Invalid public key format: " + e.getMessage());
    }
  }

  private List<AuthMethod> combineAuthMethods(
      List<AuthMethod> existingAuthMethods, AuthMethod newAuthMethod) {

    if (existingAuthMethods == null || existingAuthMethods.isEmpty()) {
      return List.of(newAuthMethod);
    }

    if (existingAuthMethods.contains(newAuthMethod)) {
      return List.copyOf(existingAuthMethods);
    }

    return Stream.concat(existingAuthMethods.stream(), Stream.of(newAuthMethod)).toList();
  }

  private Single<BiometricTokenResponseDto> generateTokensForUser(
      RefreshTokenModel refreshTokenModel,
      MultivaluedMap<String, String> headers,
      String tenantId,
      List<AuthMethod> authMethods) {

    return userService
        .getUser(Map.of(USERID, refreshTokenModel.getUserId()), headers, tenantId)
        .flatMap(
            userResponse ->
                authorizationService.generateMfaSignInTokens(
                    userResponse,
                    refreshTokenModel.getRefreshToken(),
                    refreshTokenModel.getScope(),
                    authMethods,
                    refreshTokenModel.getClientId(),
                    tenantId))
        .map(
            tokenResponse ->
                new BiometricTokenResponseDto(
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getIdToken(),
                    tokenResponse.getTokenType(),
                    tokenResponse.getExpiresIn(),
                    tokenResponse.getIsNewUser(),
                    tokenResponse.getMfaFactors()));
  }
}
