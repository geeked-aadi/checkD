package com.example.eventattendance.sync;

import com.example.eventattendance.domain.repository.AttendanceMatrixRepository;
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
public final class SyncManager_Factory implements Factory<SyncManager> {
  private final Provider<NetworkConnectivityObserver> connectivityObserverProvider;

  private final Provider<AttendanceRepository> attendanceRepositoryProvider;

  private final Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider;

  private SyncManager_Factory(Provider<NetworkConnectivityObserver> connectivityObserverProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider) {
    this.connectivityObserverProvider = connectivityObserverProvider;
    this.attendanceRepositoryProvider = attendanceRepositoryProvider;
    this.attendanceMatrixRepositoryProvider = attendanceMatrixRepositoryProvider;
  }

  @Override
  public SyncManager get() {
    return newInstance(connectivityObserverProvider.get(), attendanceRepositoryProvider, attendanceMatrixRepositoryProvider);
  }

  public static SyncManager_Factory create(
      Provider<NetworkConnectivityObserver> connectivityObserverProvider,
      Provider<AttendanceRepository> attendanceRepositoryProvider,
      Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider) {
    return new SyncManager_Factory(connectivityObserverProvider, attendanceRepositoryProvider, attendanceMatrixRepositoryProvider);
  }

  public static SyncManager newInstance(NetworkConnectivityObserver connectivityObserver,
      javax.inject.Provider<AttendanceRepository> attendanceRepositoryProvider,
      javax.inject.Provider<AttendanceMatrixRepository> attendanceMatrixRepositoryProvider) {
    return new SyncManager(connectivityObserver, attendanceRepositoryProvider, attendanceMatrixRepositoryProvider);
  }
}
