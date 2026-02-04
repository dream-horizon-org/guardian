package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.SCOPE_DELIMITER;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.MAX_LOGIN_ATTEMPTS_EXCEEDED;
import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

import com.dreamsportslabs.guardian.cache.DefaultClientScopesCache;
import com.dreamsportslabs.guardian.config.tenant.PasswordPinBlockConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.dreamsportslabs.guardian.dao.PasswordPinDao;
import com.dreamsportslabs.guardian.dao.UserFlowBlockDao;
import com.dreamsportslabs.guardian.dao.model.UserFlowBlockModel;
import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.dto.request.V1SignInRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1SignUpRequestDto;
import com.dreamsportslabs.guardian.dto.request.v2.V2SignInUpRequestDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class PasswordAuth {

  private final UserService userService;
  private final AuthorizationService authorizationService;
  private final ClientService clientService;
  private final UserFlowBlockService userFlowBlockService;
  private final DefaultClientScopesCache defaultClientScopesCache;
  private final PasswordPinDao passwordPinDao;
  private final UserFlowBlockDao userFlowBlockDao;
  private final Registry registry;

  /**
   * Common block flow for sign-in and MFA sign-in: check block → authenticate → clear wrong
   * attempts on success → handle wrong credentials on error. Returns authenticated user.
   */
  public Single<JsonObject> authenticateWithBlockFlow(
      String tenantId,
      String userIdentifier,
      BlockFlow blockFlow,
      Single<JsonObject> authenticateAction) {
    BlockConfig config = resolveBlockConfig(tenantId);
    return userFlowBlockService
        .isFlowBlocked(tenantId, List.of(userIdentifier), blockFlow)
        .andThen(authenticateAction)
        .flatMap(
            user ->
                passwordPinDao
                    .deleteWrongAttemptsCount(tenantId, userIdentifier, blockFlow)
                    .andThen(Single.just(user)))
        .onErrorResumeNext(
            error ->
                handleWrongCredentialsError(tenantId, userIdentifier, blockFlow, config, error));
  }

  /**
   * Handle wrong credentials: increment counter and maybe block. Re-throws non-credential errors.
   */
  private Single<JsonObject> handleWrongCredentialsError(
      String tenantId,
      String userIdentifier,
      BlockFlow blockFlow,
      BlockConfig config,
      Throwable error) {
    if (!isWrongCredentialsError(error)) {
      return Single.error(error);
    }
    return handleWrongAttemptAndMaybeBlock(tenantId, userIdentifier, blockFlow, config, error);
  }

  public Single<Object> signIn(
      V1SignInRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    String userIdentifier = dto.getUsername();
    Single<JsonObject> authenticateAction =
        userService.authenticate(
            UserDto.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .additionalInfo(dto.getAdditionalInfo())
                .build(),
            headers,
            tenantId);
    return authenticateWithBlockFlow(
            tenantId, userIdentifier, BlockFlow.PASSWORD, authenticateAction)
        .flatMap(
            user ->
                defaultClientScopesCache
                    .getDefaultClientScopes(tenantId)
                    .flatMap(
                        pair -> {
                          String clientId = pair.getLeft();
                          String scopes = String.join(SCOPE_DELIMITER, pair.getRight());
                          return authorizationService.generate(
                              user,
                              dto.getResponseType(),
                              scopes,
                              List.of(AuthMethod.PASSWORD),
                              dto.getMetaInfo(),
                              clientId,
                              tenantId);
                        }));
  }

  public Single<Object> signUp(
      V1SignUpRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {
    return userFlowBlockService
        .isFlowBlocked(tenantId, List.of(dto.getUsername()), BlockFlow.PASSWORD)
        .andThen(
            userService.createUser(
                UserDto.builder()
                    .username(dto.getUsername())
                    .password(dto.getPassword())
                    .additionalInfo(dto.getAdditionalInfo())
                    .build(),
                headers,
                tenantId))
        .flatMap(
            user ->
                defaultClientScopesCache
                    .getDefaultClientScopes(tenantId)
                    .flatMap(
                        pair -> {
                          String clientId = pair.getLeft();
                          String scopes = String.join(SCOPE_DELIMITER, pair.getRight());
                          return authorizationService.generate(
                              user,
                              dto.getResponseType(),
                              scopes,
                              List.of(AuthMethod.PASSWORD),
                              dto.getMetaInfo(),
                              clientId,
                              tenantId);
                        }));
  }

  public Single<Object> v2SignIn(
      V2SignInUpRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {
    AuthMethod authMethod =
        StringUtils.isBlank(requestDto.getPassword())
            ? AuthMethod.PIN_OR_PATTERN
            : AuthMethod.PASSWORD;
    BlockFlow blockFlow = authMethod == AuthMethod.PASSWORD ? BlockFlow.PASSWORD : BlockFlow.PIN;
    String userIdentifier =
        Stream.of(requestDto.getUsername(), requestDto.getEmail(), requestDto.getPhoneNumber())
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(requestDto.getUsername());

    Single<JsonObject> authenticateAction =
        userService.authenticate(buildUserDto(requestDto), headers, tenantId);
    return clientService
        .validateFirstPartyClientAndClientScopes(
            tenantId, requestDto.getClientId(), requestDto.getScopes())
        .andThen(
            authenticateWithBlockFlow(tenantId, userIdentifier, blockFlow, authenticateAction)
                .flatMap(
                    user ->
                        authorizationService.generate(
                            user,
                            requestDto.getResponseType().getResponseType(),
                            String.join(SCOPE_DELIMITER, requestDto.getScopes()),
                            List.of(authMethod),
                            requestDto.getMetaInfo(),
                            requestDto.getClientId(),
                            tenantId)));
  }

  public Single<Object> v2SignUp(
      V2SignInUpRequestDto requestDto, MultivaluedMap<String, String> headers, String tenantId) {
    AuthMethod authMethod =
        StringUtils.isBlank(requestDto.getPassword())
            ? AuthMethod.PIN_OR_PATTERN
            : AuthMethod.PASSWORD;

    String userIdentifier =
        StringUtils.isNotBlank(requestDto.getUsername())
            ? requestDto.getUsername()
            : (StringUtils.isNotBlank(requestDto.getEmail())
                ? requestDto.getEmail()
                : requestDto.getPhoneNumber());

    return clientService
        .validateFirstPartyClientAndClientScopes(
            tenantId, requestDto.getClientId(), requestDto.getScopes())
        .andThen(
            userFlowBlockService.isFlowBlocked(
                tenantId, List.of(userIdentifier), BlockFlow.PASSWORD))
        .andThen(userService.createUser(buildUserDto(requestDto), headers, tenantId))
        .flatMap(
            user ->
                authorizationService.generate(
                    user,
                    requestDto.getResponseType().getResponseType(),
                    String.join(SCOPE_DELIMITER, requestDto.getScopes()),
                    List.of(authMethod),
                    requestDto.getMetaInfo(),
                    requestDto.getClientId(),
                    tenantId));
  }

  private static boolean isWrongCredentialsError(Throwable error) {
    if (!(error instanceof WebApplicationException wae)) return false;
    return wae.getResponse().getStatus() == SC_BAD_REQUEST;
  }

  private BlockConfig resolveBlockConfig(String tenantId) {
    PasswordPinBlockConfig config =
        registry
            .get(tenantId, TenantConfig.class)
            .findPasswordPinBlockConfig()
            .orElseGet(() -> PasswordPinBlockConfig.builder().build());
    return new BlockConfig(
        config.getAttemptsAllowed(),
        config.getAttemptsWindowSeconds(),
        config.getBlockIntervalSeconds());
  }

  private <T> Single<T> handleWrongAttemptAndMaybeBlock(
      String tenantId,
      String userIdentifier,
      BlockFlow blockFlow,
      BlockConfig config,
      Throwable error) {
    return passwordPinDao
        .incrementWrongAttemptsCount(tenantId, userIdentifier, config.windowSeconds, blockFlow)
        .andThen(passwordPinDao.getWrongAttemptsCount(tenantId, userIdentifier, blockFlow))
        .flatMap(
            newWrongAttemptsCount -> {
              if (newWrongAttemptsCount >= config.maxAttempts) {
                return blockUserAndCleanup(
                    userIdentifier, tenantId, blockFlow, config.blockIntervalSeconds);
              }
              return Single.error(error);
            });
  }

  private <T> Single<T> blockUserAndCleanup(
      String userIdentifier, String tenantId, BlockFlow blockFlow, int blockIntervalSeconds) {
    long unblockedAt = getCurrentTimeInSeconds() + blockIntervalSeconds;
    String blockReason = "Maximum password/PIN login attempts limit exceeded";

    UserFlowBlockModel blockModel =
        UserFlowBlockModel.builder()
            .tenantId(tenantId)
            .userIdentifier(userIdentifier)
            .flowName(blockFlow.getFlowName())
            .reason(blockReason)
            .unblockedAt(unblockedAt)
            .isActive(true)
            .build();

    return userFlowBlockDao
        .blockFlows(List.of(blockModel))
        .andThen(passwordPinDao.deleteWrongAttemptsCount(tenantId, userIdentifier, blockFlow))
        .andThen(
            Single.error(
                MAX_LOGIN_ATTEMPTS_EXCEEDED.getCustomException(
                    Map.of("retry_after", unblockedAt))));
  }

  private record BlockConfig(int maxAttempts, int windowSeconds, int blockIntervalSeconds) {}

  private UserDto buildUserDto(V2SignInUpRequestDto requestDto) {
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

    userDtoBuilder.additionalInfo(requestDto.getAdditionalInfo());
    return userDtoBuilder.build();
  }
}
