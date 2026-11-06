package com.example.eventattendance.domain.usecase;

import com.example.eventattendance.domain.repository.AuthRepository;
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
public final class LogoutUseCase_Factory implements Factory<LogoutUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private LogoutUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public LogoutUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static LogoutUseCase_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new LogoutUseCase_Factory(authRepositoryProvider);
  }

  public static LogoutUseCase newInstance(AuthRepository authRepository) {
    return new LogoutUseCase(authRepository);
  }
}
