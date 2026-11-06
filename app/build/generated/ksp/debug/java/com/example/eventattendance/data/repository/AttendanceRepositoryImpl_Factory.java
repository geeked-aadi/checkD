package com.example.eventattendance.data.repository;

import android.content.Context;
import com.example.eventattendance.data.remote.datasource.RpcRemoteDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "dagger.hilt.android.qualifiers.ApplicationContext",
    "com.example.eventattendance.di.IoDispatcher"
})
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
public final class AttendanceRepositoryImpl_Factory implements Factory<AttendanceRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<RpcRemoteDataSource> rpcRemoteDataSourceProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  private AttendanceRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<RpcRemoteDataSource> rpcRemoteDataSourceProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.contextProvider = contextProvider;
    this.rpcRemoteDataSourceProvider = rpcRemoteDataSourceProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public AttendanceRepositoryImpl get() {
    return newInstance(contextProvider.get(), rpcRemoteDataSourceProvider.get(), ioDispatcherProvider.get());
  }

  public static AttendanceRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<RpcRemoteDataSource> rpcRemoteDataSourceProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new AttendanceRepositoryImpl_Factory(contextProvider, rpcRemoteDataSourceProvider, ioDispatcherProvider);
  }

  public static AttendanceRepositoryImpl newInstance(Context context,
      RpcRemoteDataSource rpcRemoteDataSource, CoroutineDispatcher ioDispatcher) {
    return new AttendanceRepositoryImpl(context, rpcRemoteDataSource, ioDispatcher);
  }
}
