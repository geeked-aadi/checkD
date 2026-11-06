package com.example.eventattendance.domain.usecase;

import com.example.eventattendance.domain.repository.ParticipantCacheRepository;
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
public final class LookupParticipantUseCase_Factory implements Factory<LookupParticipantUseCase> {
  private final Provider<ParticipantCacheRepository> participantCacheRepositoryProvider;

  private LookupParticipantUseCase_Factory(
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider) {
    this.participantCacheRepositoryProvider = participantCacheRepositoryProvider;
  }

  @Override
  public LookupParticipantUseCase get() {
    return newInstance(participantCacheRepositoryProvider.get());
  }

  public static LookupParticipantUseCase_Factory create(
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider) {
    return new LookupParticipantUseCase_Factory(participantCacheRepositoryProvider);
  }

  public static LookupParticipantUseCase newInstance(
      ParticipantCacheRepository participantCacheRepository) {
    return new LookupParticipantUseCase(participantCacheRepository);
  }
}
