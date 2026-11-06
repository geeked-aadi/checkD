package com.example.eventattendance.sync;

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

  private SyncManager_Factory(Provider<NetworkConnectivityObserver> connectivityObserverProvider) {
    this.connectivityObserverProvider = connectivityObserverProvider;
  }

  @Override
  public SyncManager get() {
    return newInstance(connectivityObserverProvider.get());
  }

  public static SyncManager_Factory create(
      Provider<NetworkConnectivityObserver> connectivityObserverProvider) {
    return new SyncManager_Factory(connectivityObserverProvider);
  }

  public static SyncManager newInstance(NetworkConnectivityObserver connectivityObserver) {
    return new SyncManager(connectivityObserver);
  }
}
