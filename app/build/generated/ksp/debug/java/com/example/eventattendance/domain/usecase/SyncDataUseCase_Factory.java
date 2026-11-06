package com.example.eventattendance.domain.usecase;

import com.example.eventattendance.domain.repository.SyncRepository;
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
public final class SyncDataUseCase_Factory implements Factory<SyncDataUseCase> {
  private final Provider<SyncRepository> syncRepositoryProvider;

  private SyncDataUseCase_Factory(Provider<SyncRepository> syncRepositoryProvider) {
    this.syncRepositoryProvider = syncRepositoryProvider;
  }

  @Override
  public SyncDataUseCase get() {
    return newInstance(syncRepositoryProvider.get());
  }

  public static SyncDataUseCase_Factory create(Provider<SyncRepository> syncRepositoryProvider) {
    return new SyncDataUseCase_Factory(syncRepositoryProvider);
  }

  public static SyncDataUseCase newInstance(SyncRepository syncRepository) {
    return new SyncDataUseCase(syncRepository);
  }
}
