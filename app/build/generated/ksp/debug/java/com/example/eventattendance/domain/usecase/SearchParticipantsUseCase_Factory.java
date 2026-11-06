package com.example.eventattendance.domain.usecase;

import com.example.eventattendance.domain.repository.ParticipantRepository;
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
public final class SearchParticipantsUseCase_Factory implements Factory<SearchParticipantsUseCase> {
  private final Provider<ParticipantRepository> participantRepositoryProvider;

  private SearchParticipantsUseCase_Factory(
      Provider<ParticipantRepository> participantRepositoryProvider) {
    this.participantRepositoryProvider = participantRepositoryProvider;
  }

  @Override
  public SearchParticipantsUseCase get() {
    return newInstance(participantRepositoryProvider.get());
  }

  public static SearchParticipantsUseCase_Factory create(
      Provider<ParticipantRepository> participantRepositoryProvider) {
    return new SearchParticipantsUseCase_Factory(participantRepositoryProvider);
  }

  public static SearchParticipantsUseCase newInstance(ParticipantRepository participantRepository) {
    return new SearchParticipantsUseCase(participantRepository);
  }
}
