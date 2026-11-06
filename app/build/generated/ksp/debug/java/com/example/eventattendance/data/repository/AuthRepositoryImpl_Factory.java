package com.example.eventattendance.data.repository;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import com.example.eventattendance.data.remote.datasource.AuthRemoteDataSource;
import com.example.eventattendance.sync.ConnectivityObserver;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.example.eventattendance.di.IoDispatcher")
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<AuthRemoteDataSource> authRemoteDataSourceProvider;

  private final Provider<DataStore<Preferences>> dataStoreProvider;

  private final Provider<ConnectivityObserver> connectivityObserverProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  private AuthRepositoryImpl_Factory(Provider<AuthRemoteDataSource> authRemoteDataSourceProvider,
      Provider<DataStore<Preferences>> dataStoreProvider,
      Provider<ConnectivityObserver> connectivityObserverProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.authRemoteDataSourceProvider = authRemoteDataSourceProvider;
    this.dataStoreProvider = dataStoreProvider;
    this.connectivityObserverProvider = connectivityObserverProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(authRemoteDataSourceProvider.get(), dataStoreProvider.get(), connectivityObserverProvider.get(), ioDispatcherProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(
      Provider<AuthRemoteDataSource> authRemoteDataSourceProvider,
      Provider<DataStore<Preferences>> dataStoreProvider,
      Provider<ConnectivityObserver> connectivityObserverProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new AuthRepositoryImpl_Factory(authRemoteDataSourceProvider, dataStoreProvider, connectivityObserverProvider, ioDispatcherProvider);
  }

  public static AuthRepositoryImpl newInstance(AuthRemoteDataSource authRemoteDataSource,
      DataStore<Preferences> dataStore, ConnectivityObserver connectivityObserver,
      CoroutineDispatcher ioDispatcher) {
    return new AuthRepositoryImpl(authRemoteDataSource, dataStore, connectivityObserver, ioDispatcher);
  }
}
