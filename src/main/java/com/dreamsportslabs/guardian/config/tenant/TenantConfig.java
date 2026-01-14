package com.dreamsportslabs.guardian.config.tenant;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.ADMIN_NOT_CONFIGURED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.AUTH_CODE_NOT_CONFIGURED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.CONTACT_VERIFY_NOT_CONFIGURED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.EMAIL_NOT_CONFIGURED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.FACEBOOK_AUTH_NOT_CONFIGURED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GOOGLE_AUTH_NOT_CONFIGURED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.GUEST_LOGIN_NOT_CONFIGURED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_CONFIG_NOT_EXISTS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OIDC_PROVIDER_NOT_CONFIGURED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.OTP_NOT_CONFIGURED;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.SMS_NOT_CONFIGURED;

import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Builder
public class TenantConfig {

  @Getter private String tenantId;
  @Getter private UserConfig userConfig;
  @Getter private TokenConfig tokenConfig;

  private AuthCodeConfig authCodeConfig;
  private EmailConfig emailConfig;
  private FbConfig fbConfig;
  private GoogleConfig googleConfig;
  private SmsConfig smsConfig;
  private OtpConfig otpConfig;
  private OidcConfig oidcConfig;
  private ContactVerifyConfig contactVerifyConfig;
  private Map<String, OidcProviderConfig> oidcProviderConfig;
  private AdminConfig adminConfig;
  private GuestConfig guestConfig;

  public AuthCodeConfig getAuthCodeConfig() {
    if (authCodeConfig == null) {
      throw AUTH_CODE_NOT_CONFIGURED.getException();
    }
    return authCodeConfig;
  }

  public EmailConfig getEmailConfig() {
    if (emailConfig == null) {
      throw EMAIL_NOT_CONFIGURED.getException();
    }
    return emailConfig;
  }

  public FbConfig getFbConfig() {
    if (fbConfig == null) {
      throw FACEBOOK_AUTH_NOT_CONFIGURED.getException();
    }
    return fbConfig;
  }

  public GoogleConfig getGoogleConfig() {
    if (googleConfig == null) {
      throw GOOGLE_AUTH_NOT_CONFIGURED.getException();
    }
    return googleConfig;
  }

  public SmsConfig getSmsConfig() {
    if (smsConfig == null) {
      throw SMS_NOT_CONFIGURED.getException();
    }
    return smsConfig;
  }

  public OtpConfig getOtpConfig() {
    if (otpConfig == null) {
      throw OTP_NOT_CONFIGURED.getException();
    }
    return otpConfig;
  }

  public OidcConfig getOidcConfig() {
    if (oidcConfig == null) {
      throw OIDC_CONFIG_NOT_EXISTS.getException();
    }
    return oidcConfig;
  }

  public ContactVerifyConfig getContactVerifyConfig() {
    if (contactVerifyConfig == null) {
      throw CONTACT_VERIFY_NOT_CONFIGURED.getException();
    }
    return contactVerifyConfig;
  }

  public Map<String, OidcProviderConfig> getOidcProviderConfig() {
    if (oidcProviderConfig == null) {
      throw OIDC_PROVIDER_NOT_CONFIGURED.getException();
    }
    return oidcProviderConfig;
  }

  public AdminConfig getAdminConfig() {
    if (adminConfig == null) {
      throw ADMIN_NOT_CONFIGURED.getException();
    }
    return adminConfig;
  }

  public GuestConfig getGuestConfig() {
    if (guestConfig == null) {
      throw GUEST_LOGIN_NOT_CONFIGURED.getException();
    }
    return guestConfig;
  }

  public Optional<FbConfig> findFbConfig() {
    return Optional.ofNullable(fbConfig);
  }

  public Optional<GoogleConfig> findGoogleConfig() {
    return Optional.ofNullable(googleConfig);
  }
}
