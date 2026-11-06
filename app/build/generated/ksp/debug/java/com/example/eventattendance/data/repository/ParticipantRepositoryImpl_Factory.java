package com.example.eventattendance.data.repository;

import com.example.eventattendance.domain.repository.ParticipantCacheRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ParticipantRepositoryImpl_Factory implements Factory<ParticipantRepositoryImpl> {
  private final Provider<ParticipantCacheRepository> participantCacheRepositoryProvider;

  private ParticipantRepositoryImpl_Factory(
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider) {
    this.participantCacheRepositoryProvider = participantCacheRepositoryProvider;
  }

  @Override
  public ParticipantRepositoryImpl get() {
    return newInstance(participantCacheRepositoryProvider.get());
  }

  public static ParticipantRepositoryImpl_Factory create(
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider) {
    return new ParticipantRepositoryImpl_Factory(participantCacheRepositoryProvider);
  }

  public static ParticipantRepositoryImpl newInstance(
      ParticipantCacheRepository participantCacheRepository) {
    return new ParticipantRepositoryImpl(participantCacheRepository);
  }
}
