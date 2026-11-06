package com.example.eventattendance.data.remote.datasource;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class SyncRemoteDataSource_Factory implements Factory<SyncRemoteDataSource> {
  private final Provider<SupabaseClient> supabaseClientProvider;

  private SyncRemoteDataSource_Factory(Provider<SupabaseClient> supabaseClientProvider) {
    this.supabaseClientProvider = supabaseClientProvider;
  }

  @Override
  public SyncRemoteDataSource get() {
    return newInstance(supabaseClientProvider.get());
  }

  public static SyncRemoteDataSource_Factory create(
      Provider<SupabaseClient> supabaseClientProvider) {
    return new SyncRemoteDataSource_Factory(supabaseClientProvider);
  }

  public static SyncRemoteDataSource newInstance(SupabaseClient supabaseClient) {
    return new SyncRemoteDataSource(supabaseClient);
  }
}
