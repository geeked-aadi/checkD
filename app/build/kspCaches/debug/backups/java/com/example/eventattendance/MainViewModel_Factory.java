package com.example.eventattendance;

import com.example.eventattendance.domain.repository.AuthRepository;
import com.example.eventattendance.domain.repository.ParticipantCacheRepository;
import com.example.eventattendance.domain.repository.UserPreferencesRepository;
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<ParticipantCacheRepository> participantCacheRepositoryProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  private MainViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.participantCacheRepositoryProvider = participantCacheRepositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(authRepositoryProvider.get(), participantCacheRepositoryProvider.get(), preferencesRepositoryProvider.get());
  }

  public static MainViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new MainViewModel_Factory(authRepositoryProvider, participantCacheRepositoryProvider, preferencesRepositoryProvider);
  }

  public static MainViewModel newInstance(AuthRepository authRepository,
      ParticipantCacheRepository participantCacheRepository,
      UserPreferencesRepository preferencesRepository) {
    return new MainViewModel(authRepository, participantCacheRepository, preferencesRepository);
  }
}
