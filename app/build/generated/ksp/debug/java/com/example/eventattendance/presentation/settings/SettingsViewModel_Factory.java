package com.example.eventattendance.presentation.settings;

import com.example.eventattendance.domain.repository.AuthRepository;
import com.example.eventattendance.domain.repository.ParticipantCacheRepository;
import com.example.eventattendance.domain.repository.UserPreferencesRepository;
import com.example.eventattendance.domain.usecase.ClearLocalCacheUseCase;
import com.example.eventattendance.domain.usecase.LogoutUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<LogoutUseCase> logoutUseCaseProvider;

  private final Provider<ParticipantCacheRepository> participantCacheRepositoryProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private final Provider<ClearLocalCacheUseCase> clearLocalCacheUseCaseProvider;

  private SettingsViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<LogoutUseCase> logoutUseCaseProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<ClearLocalCacheUseCase> clearLocalCacheUseCaseProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.logoutUseCaseProvider = logoutUseCaseProvider;
    this.participantCacheRepositoryProvider = participantCacheRepositoryProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
    this.clearLocalCacheUseCaseProvider = clearLocalCacheUseCaseProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(authRepositoryProvider.get(), logoutUseCaseProvider.get(), participantCacheRepositoryProvider.get(), userPreferencesRepositoryProvider.get(), clearLocalCacheUseCaseProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<LogoutUseCase> logoutUseCaseProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<ClearLocalCacheUseCase> clearLocalCacheUseCaseProvider) {
    return new SettingsViewModel_Factory(authRepositoryProvider, logoutUseCaseProvider, participantCacheRepositoryProvider, userPreferencesRepositoryProvider, clearLocalCacheUseCaseProvider);
  }

  public static SettingsViewModel newInstance(AuthRepository authRepository,
      LogoutUseCase logoutUseCase, ParticipantCacheRepository participantCacheRepository,
      UserPreferencesRepository userPreferencesRepository,
      ClearLocalCacheUseCase clearLocalCacheUseCase) {
    return new SettingsViewModel(authRepository, logoutUseCase, participantCacheRepository, userPreferencesRepository, clearLocalCacheUseCase);
  }
}
