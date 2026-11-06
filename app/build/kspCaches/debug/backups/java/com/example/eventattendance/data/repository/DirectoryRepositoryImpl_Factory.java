package com.example.eventattendance.data.repository;

import com.example.eventattendance.domain.repository.ParticipantCacheRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class DirectoryRepositoryImpl_Factory implements Factory<DirectoryRepositoryImpl> {
  private final Provider<ParticipantCacheRepository> participantCacheRepositoryProvider;

  private final Provider<SupabaseClient> supabaseClientProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  private DirectoryRepositoryImpl_Factory(
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<SupabaseClient> supabaseClientProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.participantCacheRepositoryProvider = participantCacheRepositoryProvider;
    this.supabaseClientProvider = supabaseClientProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public DirectoryRepositoryImpl get() {
    return newInstance(participantCacheRepositoryProvider.get(), supabaseClientProvider.get(), ioDispatcherProvider.get());
  }

  public static DirectoryRepositoryImpl_Factory create(
      Provider<ParticipantCacheRepository> participantCacheRepositoryProvider,
      Provider<SupabaseClient> supabaseClientProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new DirectoryRepositoryImpl_Factory(participantCacheRepositoryProvider, supabaseClientProvider, ioDispatcherProvider);
  }

  public static DirectoryRepositoryImpl newInstance(
      ParticipantCacheRepository participantCacheRepository, SupabaseClient supabaseClient,
      CoroutineDispatcher ioDispatcher) {
    return new DirectoryRepositoryImpl(participantCacheRepository, supabaseClient, ioDispatcher);
  }
}
