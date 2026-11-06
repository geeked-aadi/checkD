package com.example.eventattendance.data.repository;

import com.example.eventattendance.sync.NetworkConnectivityObserver;
import com.example.eventattendance.sync.SyncManager;
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
public final class SyncRepositoryImpl_Factory implements Factory<SyncRepositoryImpl> {
  private final Provider<SyncManager> syncManagerProvider;

  private final Provider<NetworkConnectivityObserver> connectivityObserverProvider;

  private SyncRepositoryImpl_Factory(Provider<SyncManager> syncManagerProvider,
      Provider<NetworkConnectivityObserver> connectivityObserverProvider) {
    this.syncManagerProvider = syncManagerProvider;
    this.connectivityObserverProvider = connectivityObserverProvider;
  }

  @Override
  public SyncRepositoryImpl get() {
    return newInstance(syncManagerProvider.get(), connectivityObserverProvider.get());
  }

  public static SyncRepositoryImpl_Factory create(Provider<SyncManager> syncManagerProvider,
      Provider<NetworkConnectivityObserver> connectivityObserverProvider) {
    return new SyncRepositoryImpl_Factory(syncManagerProvider, connectivityObserverProvider);
  }

  public static SyncRepositoryImpl newInstance(SyncManager syncManager,
      NetworkConnectivityObserver connectivityObserver) {
    return new SyncRepositoryImpl(syncManager, connectivityObserver);
  }
}
