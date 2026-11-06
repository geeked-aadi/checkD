package com.example.eventattendance.presentation.authentication;

import com.example.eventattendance.domain.usecase.StaffLoginUseCase;
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
public final class StaffAuthViewModel_Factory implements Factory<StaffAuthViewModel> {
  private final Provider<StaffLoginUseCase> staffLoginUseCaseProvider;

  private StaffAuthViewModel_Factory(Provider<StaffLoginUseCase> staffLoginUseCaseProvider) {
    this.staffLoginUseCaseProvider = staffLoginUseCaseProvider;
  }

  @Override
  public StaffAuthViewModel get() {
    return newInstance(staffLoginUseCaseProvider.get());
  }

  public static StaffAuthViewModel_Factory create(
      Provider<StaffLoginUseCase> staffLoginUseCaseProvider) {
    return new StaffAuthViewModel_Factory(staffLoginUseCaseProvider);
  }

  public static StaffAuthViewModel newInstance(StaffLoginUseCase staffLoginUseCase) {
    return new StaffAuthViewModel(staffLoginUseCase);
  }
}
