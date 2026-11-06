package com.example.eventattendance.domain.usecase;

import com.example.eventattendance.domain.repository.EventRepository;
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
public final class GetEventsWithStatsUseCase_Factory implements Factory<GetEventsWithStatsUseCase> {
  private final Provider<EventRepository> eventRepositoryProvider;

  private GetEventsWithStatsUseCase_Factory(Provider<EventRepository> eventRepositoryProvider) {
    this.eventRepositoryProvider = eventRepositoryProvider;
  }

  @Override
  public GetEventsWithStatsUseCase get() {
    return newInstance(eventRepositoryProvider.get());
  }

  public static GetEventsWithStatsUseCase_Factory create(
      Provider<EventRepository> eventRepositoryProvider) {
    return new GetEventsWithStatsUseCase_Factory(eventRepositoryProvider);
  }

  public static GetEventsWithStatsUseCase newInstance(EventRepository eventRepository) {
    return new GetEventsWithStatsUseCase(eventRepository);
  }
}
