package com.example.eventattendance.presentation.home;

import com.example.eventattendance.domain.repository.AttendanceRepository;
import com.example.eventattendance.domain.usecase.SyncDataUseCase;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private final Provider<SyncDataUseCase> syncDataUseCaseProvider;

  private HomeViewModel_Factory(Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<SyncDataUseCase> syncDataUseCaseProvider) {
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.syncDataUseCaseProvider = syncDataUseCaseProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(attendanceRepositoryProvider.get(), syncDataUseCaseProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<SyncDataUseCase> syncDataUseCaseProvider) {
    return new HomeViewModel_Factory(attendanceRepositoryProvider, syncDataUseCaseProvider);
  }

  public static HomeViewModel newInstance(AttendanceRepository attendanceRepository,
      SyncDataUseCase syncDataUseCase) {
    return new HomeViewModel(attendanceRepository, syncDataUseCase);
  }
}
