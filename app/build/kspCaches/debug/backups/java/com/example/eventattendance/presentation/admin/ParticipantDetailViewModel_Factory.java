package com.example.eventattendance.presentation.admin;

import androidx.lifecycle.SavedStateHandle;
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository;
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
public final class ParticipantDetailViewModel_Factory implements Factory<ParticipantDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ParticipantCacheRepository> participantCacheRepositoryProvider;

  private final Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private ParticipantDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.participantCacheRepositoryProvider = participantCacheRepositoryProvider;
    this.attendanceMatrixRepositoryProvider = attendanceMatrixRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public ParticipantDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), participantCacheRepositoryProvider.get(), attendanceMatrixRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static ParticipantDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new ParticipantDetailViewModel_Factory(savedStateHandleProvider, participantCacheRepositoryProvider, attendanceMatrixRepositoryProvider, authRepositoryProvider);
  }

  public static ParticipantDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      ParticipantCacheRepository participantCacheRepository,
      AttendanceMatrixRepository attendanceMatrixRepository, AuthRepository authRepository) {
    return new ParticipantDetailViewModel(savedStateHandle, participantCacheRepository, attendanceMatrixRepository, authRepository);
  }
}
