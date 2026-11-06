package com.example.eventattendance.domain.usecase;

import com.example.eventattendance.domain.repository.DirectoryRepository;
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
public final class GetSpocDashboardUseCase_Factory implements Factory<GetSpocDashboardUseCase> {
  private final Provider<DirectoryRepository> directoryRepositoryProvider;

  private GetSpocDashboardUseCase_Factory(
      Provider<DirectoryRepository> directoryRepositoryProvider) {
    this.directoryRepositoryProvider = directoryRepositoryProvider;
  }

  @Override
  public GetSpocDashboardUseCase get() {
    return newInstance(directoryRepositoryProvider.get());
  }

  public static GetSpocDashboardUseCase_Factory create(
      Provider<DirectoryRepository> directoryRepositoryProvider) {
    return new GetSpocDashboardUseCase_Factory(directoryRepositoryProvider);
  }

  public static GetSpocDashboardUseCase newInstance(DirectoryRepository directoryRepository) {
    return new GetSpocDashboardUseCase(directoryRepository);
  }
}
