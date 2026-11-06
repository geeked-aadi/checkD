package com.example.eventattendance.data.repository;

import com.example.eventattendance.data.remote.datasource.EventRemoteDataSource;
import com.example.eventattendance.domain.repository.AttendanceRepository;
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
public final class EventRepositoryImpl_Factory implements Factory<EventRepositoryImpl> {
  private final Provider<EventRemoteDataSource> eventRemoteDataSourceProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private EventRepositoryImpl_Factory(Provider<EventRemoteDataSource> eventRemoteDataSourceProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider) {
    this.eventRemoteDataSourceProvider = eventRemoteDataSourceProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
  }

  @Override
  public EventRepositoryImpl get() {
    return newInstance(eventRemoteDataSourceProvider.get(), attendanceRepositoryProvider.get());
  }

  public static EventRepositoryImpl_Factory create(
      Provider<EventRemoteDataSource> eventRemoteDataSourceProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider) {
    return new EventRepositoryImpl_Factory(eventRemoteDataSourceProvider, attendanceRepositoryProvider);
  }

  public static EventRepositoryImpl newInstance(EventRemoteDataSource eventRemoteDataSource,
      AttendanceRepository attendanceRepository) {
    return new EventRepositoryImpl(eventRemoteDataSource, attendanceRepository);
  }
}
