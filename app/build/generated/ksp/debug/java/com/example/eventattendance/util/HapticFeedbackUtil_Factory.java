package com.example.eventattendance.util;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class HapticFeedbackUtil_Factory implements Factory<HapticFeedbackUtil> {
  private final Provider<Context> contextProvider;

  private HapticFeedbackUtil_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public HapticFeedbackUtil get() {
    return newInstance(contextProvider.get());
  }

  public static HapticFeedbackUtil_Factory create(Provider<Context> contextProvider) {
    return new HapticFeedbackUtil_Factory(contextProvider);
  }

  public static HapticFeedbackUtil newInstance(Context context) {
    return new HapticFeedbackUtil(context);
  }
}
