package com.example.eventattendance.presentation.admin;

import com.example.eventattendance.domain.repository.AttendanceMatrixRepository;
import com.example.eventattendance.domain.repository.AttendanceRepository;
import com.example.eventattendance.domain.repository.AuthRepository;
import com.example.eventattendance.domain.repository.DirectoryRepository;
import com.example.eventattendance.domain.repository.EventRepository;
import com.example.eventattendance.domain.repository.ParticipantCacheRepository;
import com.example.eventattendance.domain.usecase.LogoutUseCase;
import com.example.eventattendance.domain.usecase.ManageEventsUseCase;
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
public final class AdminViewModel_Factory implements Factory<AdminViewModel> {
  private final Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider;

  private final Provider<ParticipantCacheRepository> participantCacheRepositoryProvider;

  private final Provider<DirectoryRepository> directoryRepositoryProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private final Provider<EventRepository> eventRepositoryProvider;

  private final Provider<ManageEventsUseCase> manageEventsUseCaseProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<LogoutUseCase> logoutUseCaseProvider;

  private AdminViewModel_Factory(
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<DirectoryRepository> directoryRepositoryProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<ManageEventsUseCase> manageEventsUseCaseProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<LogoutUseCase> logoutUseCaseProvider) {
    this.attendanceMatrixRepositoryProvider = attendanceMatrixRepositoryProvider;
    this.participantCacheRepositoryProvider = participantCacheRepositoryProvider;
    this.directoryRepositoryProvider = directoryRepositoryProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.eventRepositoryProvider = eventRepositoryProvider;
    this.manageEventsUseCaseProvider = manageEventsUseCaseProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.logoutUseCaseProvider = logoutUseCaseProvider;
  }

  @Override
  public AdminViewModel get() {
    return newInstance(attendanceMatrixRepositoryProvider.get(), participantCacheRepositoryProvider.get(), directoryRepositoryProvider.get(), attendanceRepositoryProvider.get(), eventRepositoryProvider.get(), manageEventsUseCaseProvider.get(), authRepositoryProvider.get(), logoutUseCaseProvider.get());
  }

  public static AdminViewModel_Factory create(
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<DirectoryRepository> directoryRepositoryProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<ManageEventsUseCase> manageEventsUseCaseProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<LogoutUseCase> logoutUseCaseProvider) {
    return new AdminViewModel_Factory(attendanceMatrixRepositoryProvider, participantCacheRepositoryProvider, directoryRepositoryProvider, attendanceRepositoryProvider, eventRepositoryProvider, manageEventsUseCaseProvider, authRepositoryProvider, logoutUseCaseProvider);
  }

  public static AdminViewModel newInstance(AttendanceMatrixRepository attendanceMatrixRepository,
      ParticipantCacheRepository participantCacheRepository,
      DirectoryRepository directoryRepository, AttendanceRepository attendanceRepository,
      EventRepository eventRepository, ManageEventsUseCase manageEventsUseCase,
      AuthRepository authRepository, LogoutUseCase logoutUseCase) {
    return new AdminViewModel(attendanceMatrixRepository, participantCacheRepository, directoryRepository, attendanceRepository, eventRepository, manageEventsUseCase, authRepository, logoutUseCase);
  }
}
