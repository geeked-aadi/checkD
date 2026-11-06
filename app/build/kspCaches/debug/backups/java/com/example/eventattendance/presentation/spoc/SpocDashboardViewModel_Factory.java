package com.example.eventattendance.presentation.spoc;

import com.example.eventattendance.domain.repository.AttendanceMatrixRepository;
import com.example.eventattendance.domain.repository.AuthRepository;
import com.example.eventattendance.domain.repository.DirectoryRepository;
import com.example.eventattendance.domain.repository.ParticipantCacheRepository;
import com.example.eventattendance.domain.usecase.LogoutUseCase;
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
public final class SpocDashboardViewModel_Factory implements Factory<SpocDashboardViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<DirectoryRepository> directoryRepositoryProvider;

  private final Provider<ParticipantCacheRepository> participantCacheRepositoryProvider;

  private final Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider;

  private final Provider<LogoutUseCase> logoutUseCaseProvider;

  private SpocDashboardViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<DirectoryRepository> directoryRepositoryProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<LogoutUseCase> logoutUseCaseProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.directoryRepositoryProvider = directoryRepositoryProvider;
    this.participantCacheRepositoryProvider = participantCacheRepositoryProvider;
    this.attendanceMatrixRepositoryProvider = attendanceMatrixRepositoryProvider;
    this.logoutUseCaseProvider = logoutUseCaseProvider;
  }

  @Override
  public SpocDashboardViewModel get() {
    return newInstance(authRepositoryProvider.get(), directoryRepositoryProvider.get(), participantCacheRepositoryProvider.get(), attendanceMatrixRepositoryProvider.get(), logoutUseCaseProvider.get());
  }

  public static SpocDashboardViewModel_Factory create(
      Provider<AuthRepository> authRepositoryProvider,
      Provider<DirectoryRepository> directoryRepositoryProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<LogoutUseCase> logoutUseCaseProvider) {
    return new SpocDashboardViewModel_Factory(authRepositoryProvider, directoryRepositoryProvider, participantCacheRepositoryProvider, attendanceMatrixRepositoryProvider, logoutUseCaseProvider);
  }

  public static SpocDashboardViewModel newInstance(AuthRepository authRepository,
      DirectoryRepository directoryRepository,
      ParticipantCacheRepository participantCacheRepository,
      AttendanceMatrixRepository attendanceMatrixRepository, LogoutUseCase logoutUseCase) {
    return new SpocDashboardViewModel(authRepository, directoryRepository, participantCacheRepository, attendanceMatrixRepository, logoutUseCase);
  }
}
