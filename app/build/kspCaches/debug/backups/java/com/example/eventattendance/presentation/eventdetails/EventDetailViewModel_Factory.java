package com.example.eventattendance.presentation.eventdetails;

import androidx.lifecycle.SavedStateHandle;
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository;
import com.example.eventattendance.domain.repository.AttendanceRepository;
import com.example.eventattendance.domain.repository.AuthRepository;
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
public final class EventDetailViewModel_Factory implements Factory<EventDetailViewModel> {
  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private final Provider<ParticipantCacheRepository> participantCacheRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private EventDetailViewModel_Factory(Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.participantCacheRepositoryProvider = participantCacheRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.attendanceMatrixRepositoryProvider = attendanceMatrixRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public EventDetailViewModel get() {
    return newInstance(attendanceRepositoryProvider.get(), participantCacheRepositoryProvider.get(), authRepositoryProvider.get(), attendanceMatrixRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static EventDetailViewModel_Factory create(
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new EventDetailViewModel_Factory(attendanceRepositoryProvider, participantCacheRepositoryProvider, authRepositoryProvider, attendanceMatrixRepositoryProvider, savedStateHandleProvider);
  }

  public static EventDetailViewModel newInstance(AttendanceRepository attendanceRepository,
      ParticipantCacheRepository participantCacheRepository, AuthRepository authRepository,
      AttendanceMatrixRepository attendanceMatrixRepository, SavedStateHandle savedStateHandle) {
    return new EventDetailViewModel(attendanceRepository, participantCacheRepository, authRepository, attendanceMatrixRepository, savedStateHandle);
  }
}
