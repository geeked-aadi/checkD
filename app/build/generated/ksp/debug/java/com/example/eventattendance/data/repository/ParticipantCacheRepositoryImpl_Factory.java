package com.example.eventattendance.data.repository;

import android.content.Context;
import com.example.eventattendance.data.remote.datasource.RpcRemoteDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class ParticipantCacheRepositoryImpl_Factory implements Factory<ParticipantCacheRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<RpcRemoteDataSource> rpcRemoteDataSourceProvider;

  private final Provider<SupabaseClient> supabaseClientProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  private ParticipantCacheRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<RpcRemoteDataSource> rpcRemoteDataSourceProvider,
      Provider<SupabaseClient> supabaseClientProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.contextProvider = contextProvider;
    this.rpcRemoteDataSourceProvider = rpcRemoteDataSourceProvider;
    this.supabaseClientProvider = supabaseClientProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public ParticipantCacheRepositoryImpl get() {
    return newInstance(contextProvider.get(), rpcRemoteDataSourceProvider.get(), supabaseClientProvider.get(), ioDispatcherProvider.get());
  }

  public static ParticipantCacheRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<RpcRemoteDataSource> rpcRemoteDataSourceProvider,
      Provider<SupabaseClient> supabaseClientProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new ParticipantCacheRepositoryImpl_Factory(contextProvider, rpcRemoteDataSourceProvider, supabaseClientProvider, ioDispatcherProvider);
  }

  public static ParticipantCacheRepositoryImpl newInstance(Context context,
      RpcRemoteDataSource rpcRemoteDataSource, SupabaseClient supabaseClient,
      CoroutineDispatcher ioDispatcher) {
    return new ParticipantCacheRepositoryImpl(context, rpcRemoteDataSource, supabaseClient, ioDispatcher);
  }
}
