package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.SCOPE_DELIMITER;

import com.dreamsportslabs.guardian.cache.DefaultClientScopesCache;
import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.dreamsportslabs.guardian.dto.UserDto;
import com.dreamsportslabs.guardian.dto.request.V1SignInRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1SignUpRequestDto;
import com.dreamsportslabs.guardian.dto.request.v2.V2SignInUpRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
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

  public Single<Object> signIn(
      V1SignInRequestDto dto, MultivaluedMap<String, String> headers, String tenantId) {

    return userFlowBlockService
        .isFlowBlocked(tenantId, List.of(dto.getUsername()), BlockFlow.PASSWORD)
        .andThen(
            userService.authenticate(
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
    String userIdentifier =
        Stream.of(requestDto.getUsername(), requestDto.getEmail(), requestDto.getPhoneNumber())
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(requestDto.getUsername());

    return clientService
        .validateFirstPartyClientAndClientScopes(
            tenantId, requestDto.getClientId(), requestDto.getScopes())
        .andThen(
            userFlowBlockService.isFlowBlocked(
                tenantId, List.of(userIdentifier), BlockFlow.PASSWORD))
        .andThen(userService.authenticate(buildUserDto(requestDto), headers, tenantId))
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
