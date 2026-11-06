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
public final class RpcRemoteDataSource_Factory implements Factory<RpcRemoteDataSource> {
  private final Provider<SupabaseClient> supabaseClientProvider;

  private RpcRemoteDataSource_Factory(Provider<SupabaseClient> supabaseClientProvider) {
    this.supabaseClientProvider = supabaseClientProvider;
  }

  @Override
  public RpcRemoteDataSource get() {
    return newInstance(supabaseClientProvider.get());
  }

  public static RpcRemoteDataSource_Factory create(
      Provider<SupabaseClient> supabaseClientProvider) {
    return new RpcRemoteDataSource_Factory(supabaseClientProvider);
  }

  public static RpcRemoteDataSource newInstance(SupabaseClient supabaseClient) {
    return new RpcRemoteDataSource(supabaseClient);
  }
}
