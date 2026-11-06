package com.example.eventattendance.presentation.scanner;

import androidx.lifecycle.SavedStateHandle;
import com.example.eventattendance.domain.repository.AttendanceMatrixRepository;
import com.example.eventattendance.domain.repository.AttendanceRepository;
import com.example.eventattendance.domain.repository.AuthRepository;
import com.example.eventattendance.domain.usecase.LookupParticipantUseCase;
import com.example.eventattendance.domain.usecase.MarkAttendanceUseCase;
import com.example.eventattendance.util.AudioFeedbackUtil;
import com.example.eventattendance.util.HapticFeedbackUtil;
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
public final class ScannerViewModel_Factory implements Factory<ScannerViewModel> {
  private final Provider<LookupParticipantUseCase> lookupParticipantUseCaseProvider;

  private final Provider<MarkAttendanceUseCase> markAttendanceUseCaseProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private final Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<HapticFeedbackUtil> hapticFeedbackUtilProvider;

  private final Provider<AudioFeedbackUtil> audioFeedbackUtilProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private ScannerViewModel_Factory(
      Provider<LookupParticipantUseCase> lookupParticipantUseCaseProvider,
      Provider<MarkAttendanceUseCase> markAttendanceUseCaseProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<HapticFeedbackUtil> hapticFeedbackUtilProvider,
      Provider<AudioFeedbackUtil> audioFeedbackUtilProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.lookupParticipantUseCaseProvider = lookupParticipantUseCaseProvider;
    this.markAttendanceUseCaseProvider = markAttendanceUseCaseProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.attendanceMatrixRepositoryProvider = attendanceMatrixRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.hapticFeedbackUtilProvider = hapticFeedbackUtilProvider;
    this.audioFeedbackUtilProvider = audioFeedbackUtilProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public ScannerViewModel get() {
    return newInstance(lookupParticipantUseCaseProvider.get(), markAttendanceUseCaseProvider.get(), attendanceRepositoryProvider.get(), attendanceMatrixRepositoryProvider.get(), authRepositoryProvider.get(), hapticFeedbackUtilProvider.get(), audioFeedbackUtilProvider.get(), savedStateHandleProvider.get());
  }

  public static ScannerViewModel_Factory create(
      Provider<LookupParticipantUseCase> lookupParticipantUseCaseProvider,
      Provider<MarkAttendanceUseCase> markAttendanceUseCaseProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<HapticFeedbackUtil> hapticFeedbackUtilProvider,
      Provider<AudioFeedbackUtil> audioFeedbackUtilProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new ScannerViewModel_Factory(lookupParticipantUseCaseProvider, markAttendanceUseCaseProvider, attendanceRepositoryProvider, attendanceMatrixRepositoryProvider, authRepositoryProvider, hapticFeedbackUtilProvider, audioFeedbackUtilProvider, savedStateHandleProvider);
  }

  public static ScannerViewModel newInstance(LookupParticipantUseCase lookupParticipantUseCase,
      MarkAttendanceUseCase markAttendanceUseCase, AttendanceRepository attendanceRepository,
      AttendanceMatrixRepository attendanceMatrixRepository, AuthRepository authRepository,
      HapticFeedbackUtil hapticFeedbackUtil, AudioFeedbackUtil audioFeedbackUtil,
      SavedStateHandle savedStateHandle) {
    return new ScannerViewModel(lookupParticipantUseCase, markAttendanceUseCase, attendanceRepository, attendanceMatrixRepository, authRepository, hapticFeedbackUtil, audioFeedbackUtil, savedStateHandle);
  }
}
