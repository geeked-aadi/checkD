package com.example.eventattendance.domain.usecase;

import com.example.eventattendance.domain.repository.AttendanceRepository;
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
public final class MarkAttendanceUseCase_Factory implements Factory<MarkAttendanceUseCase> {
  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private MarkAttendanceUseCase_Factory(Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public MarkAttendanceUseCase get() {
    return newInstance(attendanceRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static MarkAttendanceUseCase_Factory create(
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new MarkAttendanceUseCase_Factory(attendanceRepositoryProvider, authRepositoryProvider);
  }

  public static MarkAttendanceUseCase newInstance(AttendanceRepository attendanceRepository,
      AuthRepository authRepository) {
    return new MarkAttendanceUseCase(attendanceRepository, authRepository);
  }
}
