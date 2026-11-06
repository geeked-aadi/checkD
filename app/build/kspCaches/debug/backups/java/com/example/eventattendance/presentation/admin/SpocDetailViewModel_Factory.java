package com.example.eventattendance.presentation.admin;

import androidx.lifecycle.SavedStateHandle;
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository;
import com.example.eventattendance.domain.repository.AuthRepository;
import com.example.eventattendance.domain.repository.DirectoryRepository;
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
public final class SpocDetailViewModel_Factory implements Factory<SpocDetailViewModel> {
  private final Provider<DirectoryRepository> directoryRepositoryProvider;

  private final Provider<ParticipantCacheRepository> participantCacheRepositoryProvider;

  private final Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private SpocDetailViewModel_Factory(Provider<DirectoryRepository> directoryRepositoryProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.directoryRepositoryProvider = directoryRepositoryProvider;
    this.participantCacheRepositoryProvider = participantCacheRepositoryProvider;
    this.attendanceMatrixRepositoryProvider = attendanceMatrixRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public SpocDetailViewModel get() {
    return newInstance(directoryRepositoryProvider.get(), participantCacheRepositoryProvider.get(), attendanceMatrixRepositoryProvider.get(), authRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static SpocDetailViewModel_Factory create(
      Provider<DirectoryRepository> directoryRepositoryProvider,
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new SpocDetailViewModel_Factory(directoryRepositoryProvider, participantCacheRepositoryProvider, attendanceMatrixRepositoryProvider, authRepositoryProvider, savedStateHandleProvider);
  }

  public static SpocDetailViewModel newInstance(DirectoryRepository directoryRepository,
      ParticipantCacheRepository participantCacheRepository,
      AttendanceMatrixRepository attendanceMatrixRepository, AuthRepository authRepository,
      SavedStateHandle savedStateHandle) {
    return new SpocDetailViewModel(directoryRepository, participantCacheRepository, attendanceMatrixRepository, authRepository, savedStateHandle);
  }
}
