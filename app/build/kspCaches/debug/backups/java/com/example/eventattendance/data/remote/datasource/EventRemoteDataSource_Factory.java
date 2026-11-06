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
public final class EventRemoteDataSource_Factory implements Factory<EventRemoteDataSource> {
  private final Provider<SupabaseClient> supabaseClientProvider;

  private EventRemoteDataSource_Factory(Provider<SupabaseClient> supabaseClientProvider) {
    this.supabaseClientProvider = supabaseClientProvider;
  }

  @Override
  public EventRemoteDataSource get() {
    return newInstance(supabaseClientProvider.get());
  }

  public static EventRemoteDataSource_Factory create(
      Provider<SupabaseClient> supabaseClientProvider) {
    return new EventRemoteDataSource_Factory(supabaseClientProvider);
  }

  public static EventRemoteDataSource newInstance(SupabaseClient supabaseClient) {
    return new EventRemoteDataSource(supabaseClient);
  }
}
