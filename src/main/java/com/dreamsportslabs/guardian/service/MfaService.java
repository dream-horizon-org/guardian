package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.EMAIL;
import static com.dreamsportslabs.guardian.constant.Constants.PASSWORD_SET;
import static com.dreamsportslabs.guardian.constant.Constants.PHONE_NUMBER;
import static com.dreamsportslabs.guardian.constant.Constants.PIN_SET;
import static com.dreamsportslabs.guardian.constant.Constants.USERID;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.MAX_RESEND_LIMIT_EXCEEDED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.MFA_FACTOR_ALREADY_ENROLLED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.MFA_FACTOR_NOT_SUPPORTED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;

import com.dreamsportslabs.guardian.config.tenant.PasswordPinBlockConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.constant.AuthMethodCategory;
import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.dreamsportslabs.guardian.constant.MfaFactor;
import com.dreamsportslabs.guardian.dao.MfaDao;
import com.dreamsportslabs.guardian.dao.UserFlowBlockDao;
import com.dreamsportslabs.guardian.dao.model.RefreshTokenModel;
import com.dreamsportslabs.guardian.dao.model.UserFlowBlockModel;
import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.dto.request.DeviceMetadataDto;
import com.dreamsportslabs.guardian.dto.request.v2.V2MfaSignInRequestDto;
import com.dreamsportslabs.guardian.dto.response.MfaFactorDto;
import com.dreamsportslabs.guardian.dto.response.TokenResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MfaService {

  private final AuthorizationService authorizationService;
  private final ClientService clientService;
  private final UserService userService;
  private final MfaDao mfaDao;
  private final UserFlowBlockService userFlowBlockService;
  private final UserFlowBlockDao userFlowBlockDao;
  private final Registry registry;

  private static final int DEFAULT_ATTEMPTS_ALLOWED = 5;
  private static final int DEFAULT_ATTEMPTS_WINDOW_SECONDS = 86400;
  private static final int DEFAULT_BLOCK_INTERVAL_SECONDS = 86400;
  private static final String DEFAULT_DEVICE_ID = "default";

  public Single<TokenResponseDto> mfaEnroll(
      V2MfaSignInRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {
    return clientService
        .validateFirstPartyClientAndClientScopes(
            tenantId, requestDto.getClientId(), requestDto.getScopes())
        .andThen(
            authorizationService.validateRefreshToken(
                tenantId, requestDto.getClientId(), requestDto.getRefreshToken()))
        .flatMap(
            refreshTokenModel ->
                validateEnrollmentRequest(refreshTokenModel, requestDto, headers, tenantId))
        .flatMap(
            refreshTokenModel ->
                enrollFactor(requestDto, headers, refreshTokenModel, tenantId)
                    .flatMap(
                        user ->
                            updateRefreshToken(
                                user,
                                requestDto.getRefreshToken(),
                                getMergedScopes(
                                    refreshTokenModel.getScope(), requestDto.getScopes()),
                                getMergedAuthMethods(
                                    refreshTokenModel.getAuthMethod(),
                                    requestDto.getFactor().getAuthMethod()),
                                requestDto.getClientId(),
                                tenantId)));
  }

  private Single<RefreshTokenModel> validateEnrollmentRequest(
      RefreshTokenModel refreshTokenModel,
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String tenantId) {
    List<AuthMethod> authMethods = refreshTokenModel.getAuthMethod();
    if (CollectionUtils.isEmpty(authMethods)) {
      return Single.error(
          INVALID_REQUEST.getCustomException(
              "Refresh token must have at least one authentication method"));
    }

    AuthMethod factorAuthMethod = requestDto.getFactor().getAuthMethod();
    if (authMethods.contains(factorAuthMethod)) {
      return Single.error(
          MFA_FACTOR_ALREADY_ENROLLED.getCustomException(
              "The factor is already enrolled in the refresh token"));
    }

    return validateIfFactorIsEnrolled(
            refreshTokenModel.getUserId(), headers, tenantId, requestDto.getFactor())
        .andThen(Single.just(refreshTokenModel));
  }

  public Single<TokenResponseDto> mfaSignIn(
      V2MfaSignInRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {
    return clientService
        .validateFirstPartyClientAndClientScopes(
            tenantId, requestDto.getClientId(), requestDto.getScopes())
        .andThen(
            validateRefreshToken(
                tenantId,
                requestDto.getClientId(),
                requestDto.getRefreshToken(),
                requestDto.getFactor()))
        .flatMap(
            refreshTokenModel -> runMfaSignIn(requestDto, headers, tenantId, refreshTokenModel));
  }

  /** Only PASSWORD and PIN factors use block logic; other factors skip block. */
  private Single<TokenResponseDto> runMfaSignIn(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String tenantId,
      RefreshTokenModel refreshTokenModel) {
    String userId = refreshTokenModel.getUserId();
    MfaFactor factor = requestDto.getFactor();
    String deviceId = resolveDeviceId(requestDto.getDeviceMetadata());
    String userIdentifier = userIdentifier(userId, deviceId);

    // Block only for password and PIN methods; other MFA methods (e.g. OTP) are not blocked
    if (factor != MfaFactor.PASSWORD && factor != MfaFactor.PIN) {
      return authenticateAndGetUserDetails(requestDto, headers, userId, tenantId)
          .flatMap(
              user ->
                  updateRefreshToken(
                      user,
                      requestDto.getRefreshToken(),
                      getMergedScopes(refreshTokenModel.getScope(), requestDto.getScopes()),
                      getMergedAuthMethods(
                          refreshTokenModel.getAuthMethod(), factor.getAuthMethod()),
                      requestDto.getClientId(),
                      tenantId));
    }

    BlockFlow blockFlow =
        factor == MfaFactor.PASSWORD ? BlockFlow.MFA_SIGNIN_PASSWORD : BlockFlow.MFA_SIGNIN_PIN;
    PasswordPinBlockConfig config =
        registry.get(tenantId, TenantConfig.class).findPasswordPinBlockConfig().orElse(null);
    int maxAttempts =
        config != null && config.getAttemptsAllowed() != null
            ? config.getAttemptsAllowed()
            : DEFAULT_ATTEMPTS_ALLOWED;
    int windowSeconds =
        config != null && config.getAttemptsWindowSeconds() != null
            ? config.getAttemptsWindowSeconds()
            : DEFAULT_ATTEMPTS_WINDOW_SECONDS;
    int blockIntervalSeconds =
        config != null && config.getBlockIntervalSeconds() != null
            ? config.getBlockIntervalSeconds()
            : DEFAULT_BLOCK_INTERVAL_SECONDS;

    return userFlowBlockService
        .isFlowBlocked(tenantId, List.of(userIdentifier), blockFlow)
        .andThen(Single.just(refreshTokenModel))
        .flatMap(
            model ->
                mfaDao
                    .getWrongAttemptsCount(tenantId, userId, deviceId, blockFlow)
                    .flatMap(
                        wrongAttemptsCount -> {
                          if (wrongAttemptsCount >= maxAttempts) {
                            return blockUserAndCleanup(
                                userId, deviceId, tenantId, blockFlow, blockIntervalSeconds);
                          }
                          return authenticateAndGetUserDetails(
                                  requestDto, headers, model.getUserId(), tenantId)
                              .flatMap(
                                  user ->
                                      mfaDao
                                          .deleteWrongAttemptsCount(
                                              tenantId, userId, deviceId, blockFlow)
                                          .andThen(
                                              updateRefreshToken(
                                                  user,
                                                  requestDto.getRefreshToken(),
                                                  getMergedScopes(
                                                      model.getScope(), requestDto.getScopes()),
                                                  getMergedAuthMethods(
                                                      model.getAuthMethod(),
                                                      requestDto.getFactor().getAuthMethod()),
                                                  requestDto.getClientId(),
                                                  tenantId)))
                              .onErrorResumeNext(
                                  error ->
                                      mfaDao
                                          .incrementWrongAttemptsCount(
                                              tenantId, userId, deviceId, windowSeconds, blockFlow)
                                          .andThen(
                                              mfaDao.getWrongAttemptsCount(
                                                  tenantId, userId, deviceId, blockFlow))
                                          .flatMap(
                                              newWrongAttemptsCount -> {
                                                if (newWrongAttemptsCount >= maxAttempts) {
                                                  return blockUserAndCleanup(
                                                      userId,
                                                      deviceId,
                                                      tenantId,
                                                      blockFlow,
                                                      blockIntervalSeconds);
                                                }
                                                return Single.error(error);
                                              }));
                        }));
  }

  private static String resolveDeviceId(DeviceMetadataDto deviceMetadata) {
    if (deviceMetadata != null && StringUtils.isNotBlank(deviceMetadata.getDeviceId())) {
      return deviceMetadata.getDeviceId();
    }
    return DEFAULT_DEVICE_ID;
  }

  private static String userIdentifier(String userId, String deviceId) {
    return userId + ":" + deviceId;
  }

  private Single<RefreshTokenModel> validateRefreshToken(
      String tenantId, String clientId, String refreshToken, MfaFactor factor) {
    return authorizationService.validateRefreshToken(tenantId, clientId, refreshToken);
  }

  private Single<TokenResponseDto> updateRefreshToken(
      JsonObject user,
      String refreshToken,
      List<String> scopes,
      List<AuthMethod> authMethods,
      String clientId,
      String tenantId) {
    return authorizationService.generateMfaSignInTokens(
        user, refreshToken, scopes, authMethods, clientId, tenantId);
  }

  private Single<JsonObject> authenticateAndGetUserDetails(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String userId,
      String tenantId) {
    return switch (requestDto.getFactor()) {
      case PASSWORD, PIN -> authenticateUser(requestDto, headers, userId, tenantId);
      default -> Single.error(MFA_FACTOR_NOT_SUPPORTED.getException());
    };
  }

  private Single<JsonObject> authenticateUser(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String userId,
      String tenantId) {
    UserDto userDto = buildUserDto(requestDto);

    return userService
        .authenticate(userDto, headers, tenantId)
        .filter(user -> user.getString(USERID).equals(userId))
        .switchIfEmpty(
            Single.error(
                UNAUTHORIZED.getCustomException("User identifier does not match refresh token")));
  }

  private List<String> getMergedScopes(List<String> existingScopes, List<String> newScopes) {
    LinkedHashSet<String> mergedScopes = new LinkedHashSet<>(existingScopes);
    mergedScopes.addAll(newScopes);
    return new ArrayList<>(mergedScopes);
  }

  private List<AuthMethod> getMergedAuthMethods(
      List<AuthMethod> authMethods, AuthMethod newAuthMethod) {
    List<AuthMethod> mergedAuthMethods = new ArrayList<>(authMethods);
    mergedAuthMethods.add(newAuthMethod);
    return mergedAuthMethods;
  }

  private UserDto buildUserDto(V2MfaSignInRequestDto requestDto) {
    UserDto.UserDtoBuilder userDtoBuilder = UserDto.builder();

    if (StringUtils.isNotBlank(requestDto.getUsername())) {
      userDtoBuilder.username(requestDto.getUsername());
    } else if (StringUtils.isNotBlank(requestDto.getEmail())) {
      userDtoBuilder.email(requestDto.getEmail());
    } else {
      userDtoBuilder.phoneNumber(requestDto.getPhoneNumber());
    }

    if (StringUtils.isNotBlank(requestDto.getPassword())) {
      userDtoBuilder.password(requestDto.getPassword());
    } else {
      userDtoBuilder.pin(requestDto.getPin());
    }

    return userDtoBuilder.build();
  }

  private Completable validateIfFactorIsEnrolled(
      String userId, MultivaluedMap<String, String> headers, String tenantId, MfaFactor mfaFactor) {
    return userService
        .getUser(Map.of(USERID, userId), headers, tenantId)
        .map(user -> isFactorEnabled(mfaFactor, user))
        .flatMapCompletable(
            enabled -> {
              if (!enabled) {
                return Completable.complete();
              } else {
                return Completable.error(MFA_FACTOR_ALREADY_ENROLLED.getException());
              }
            });
  }

  private Single<JsonObject> enrollFactor(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      RefreshTokenModel refreshTokenModel,
      String tenantId) {
    String userId = refreshTokenModel.getUserId();
    MfaFactor factor = requestDto.getFactor();

    return switch (factor) {
      case PASSWORD -> enrollPassword(requestDto, headers, userId, tenantId);
      case PIN -> enrollPin(requestDto, headers, userId, tenantId);
      default -> Single.error(MFA_FACTOR_NOT_SUPPORTED.getException());
    };
  }

  private Single<JsonObject> enrollPassword(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String userId,
      String tenantId) {
    return enrollFactor(
        headers, userId, tenantId, UserDto.builder().password(requestDto.getPassword()).build());
  }

  private Single<JsonObject> enrollPin(
      V2MfaSignInRequestDto requestDto,
      MultivaluedMap<String, String> headers,
      String userId,
      String tenantId) {
    return enrollFactor(
        headers, userId, tenantId, UserDto.builder().pin(requestDto.getPin()).build());
  }

  private Single<JsonObject> enrollFactor(
      MultivaluedMap<String, String> headers, String userId, String tenantId, UserDto userDto) {
    return userService
        .updateUser(userId, userDto, headers, tenantId)
        .andThen(userService.getUser(Map.of(USERID, userId), headers, tenantId));
  }

  public static Set<AuthMethodCategory> getUsedCategories(List<AuthMethod> authMethods) {
    return authMethods.stream().map(AuthMethod::getCategory).collect(Collectors.toSet());
  }

  public static List<MfaFactor> getAvailableMfaFactors(Set<AuthMethodCategory> usedCategories) {
    List<MfaFactor> availableFactors = new ArrayList<>();

    for (MfaFactor factor : MfaFactor.values()) {
      AuthMethodCategory factorCategory = factor.getAuthMethod().getCategory();
      if (!usedCategories.contains(factorCategory)) {
        availableFactors.add(factor);
      }
    }
    return availableFactors;
  }

  private static Boolean isFactorEnabled(MfaFactor factor, JsonObject user) {
    if (user == null) {
      return false;
    }

    return switch (factor) {
      case PASSWORD -> Boolean.TRUE.equals(user.getBoolean(PASSWORD_SET));
      case PIN -> Boolean.TRUE.equals(user.getBoolean(PIN_SET));
      case SMS_OTP -> hasValue(user.getString(PHONE_NUMBER));
      case EMAIL_OTP -> hasValue(user.getString(EMAIL));
      default -> false;
    };
  }

  private static boolean hasValue(String value) {
    return value != null && !value.isBlank();
  }

  private Single<TokenResponseDto> blockUserAndCleanup(
      String userId,
      String deviceId,
      String tenantId,
      BlockFlow blockFlow,
      int blockIntervalSeconds) {

    long unblockedAt = getCurrentTimeInSeconds() + blockIntervalSeconds;
    String blockReason = "Maximum MFA password/PIN wrong attempts limit exceeded";
    String compositeUserIdentifier = userIdentifier(userId, deviceId);

    UserFlowBlockModel blockModel =
        UserFlowBlockModel.builder()
            .tenantId(tenantId)
            .userIdentifier(compositeUserIdentifier)
            .flowName(blockFlow.getFlowName())
            .reason(blockReason)
            .unblockedAt(unblockedAt)
            .isActive(true)
            .build();
    return userFlowBlockDao
        .blockFlows(List.of(blockModel))
        .andThen(mfaDao.deleteWrongAttemptsCount(tenantId, userId, deviceId, blockFlow))
        .andThen(
            Single.error(
                MAX_RESEND_LIMIT_EXCEEDED.getCustomException(
                    blockReason, Map.of("retry_after", unblockedAt))));
  }

  public static List<MfaFactorDto> buildMfaFactors(
      List<AuthMethod> currentAuthMethods, JsonObject user, List<String> clientMfaMethods) {
    if (currentAuthMethods == null || currentAuthMethods.isEmpty()) {
      throw INTERNAL_SERVER_ERROR.getCustomException(
          "AuthMethods cannot be null or empty when building MFA factors");
    }

    if (clientMfaMethods == null || clientMfaMethods.isEmpty()) {
      return new ArrayList<>();
    }

    Set<AuthMethodCategory> usedCategories = getUsedCategories(currentAuthMethods);
    List<MfaFactor> availableFactors = getAvailableMfaFactors(usedCategories);

    return availableFactors.stream()
        .filter(factor -> clientMfaMethods.contains(factor.getValue()))
        .map(
            factor ->
                MfaFactorDto.builder()
                    .factor(factor.getValue())
                    .isEnabled(isFactorEnabled(factor, user))
                    .build())
        .collect(Collectors.toList());
  }
}
