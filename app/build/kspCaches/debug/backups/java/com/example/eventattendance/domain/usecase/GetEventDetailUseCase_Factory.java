package com.example.eventattendance.domain.usecase;

import com.example.eventattendance.domain.repository.AttendanceRepository;
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
public final class GetEventDetailUseCase_Factory implements Factory<GetEventDetailUseCase> {
  private final Provider<EventRepository> eventRepositoryProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private GetEventDetailUseCase_Factory(Provider<EventRepository> eventRepositoryProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider) {
    this.eventRepositoryProvider = eventRepositoryProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
  }

  @Override
  public GetEventDetailUseCase get() {
    return newInstance(eventRepositoryProvider.get(), attendanceRepositoryProvider.get());
  }

  public static GetEventDetailUseCase_Factory create(
      Provider<EventRepository> eventRepositoryProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider) {
    return new GetEventDetailUseCase_Factory(eventRepositoryProvider, attendanceRepositoryProvider);
  }

  public static GetEventDetailUseCase newInstance(EventRepository eventRepository,
      AttendanceRepository attendanceRepository) {
    return new GetEventDetailUseCase(eventRepository, attendanceRepository);
  }
}
