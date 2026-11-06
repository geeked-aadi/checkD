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
public final class StaffLoginUseCase_Factory implements Factory<StaffLoginUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private StaffLoginUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public StaffLoginUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static StaffLoginUseCase_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new StaffLoginUseCase_Factory(authRepositoryProvider);
  }

  public static StaffLoginUseCase newInstance(AuthRepository authRepository) {
    return new StaffLoginUseCase(authRepository);
  }
}
