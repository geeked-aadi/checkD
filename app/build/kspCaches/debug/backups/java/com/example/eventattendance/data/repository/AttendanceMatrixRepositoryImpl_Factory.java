package com.example.eventattendance.data.repository;

import com.example.eventattendance.data.remote.datasource.RpcRemoteDataSource;
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
public final class AttendanceMatrixRepositoryImpl_Factory implements Factory<AttendanceMatrixRepositoryImpl> {
  private final Provider<RpcRemoteDataSource> rpcRemoteDataSourceProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  private AttendanceMatrixRepositoryImpl_Factory(
      Provider<RpcRemoteDataSource> rpcRemoteDataSourceProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.rpcRemoteDataSourceProvider = rpcRemoteDataSourceProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public AttendanceMatrixRepositoryImpl get() {
    return newInstance(rpcRemoteDataSourceProvider.get(), ioDispatcherProvider.get());
  }

  public static AttendanceMatrixRepositoryImpl_Factory create(
      Provider<RpcRemoteDataSource> rpcRemoteDataSourceProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new AttendanceMatrixRepositoryImpl_Factory(rpcRemoteDataSourceProvider, ioDispatcherProvider);
  }

  public static AttendanceMatrixRepositoryImpl newInstance(RpcRemoteDataSource rpcRemoteDataSource,
      CoroutineDispatcher ioDispatcher) {
    return new AttendanceMatrixRepositoryImpl(rpcRemoteDataSource, ioDispatcher);
  }
}
