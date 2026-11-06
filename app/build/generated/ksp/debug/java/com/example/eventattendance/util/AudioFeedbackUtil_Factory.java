package com.example.eventattendance.util;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AudioFeedbackUtil_Factory implements Factory<AudioFeedbackUtil> {
  @Override
  public AudioFeedbackUtil get() {
    return newInstance();
  }

  public static AudioFeedbackUtil_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AudioFeedbackUtil newInstance() {
    return new AudioFeedbackUtil();
  }

  private static final class InstanceHolder {
    static final AudioFeedbackUtil_Factory INSTANCE = new AudioFeedbackUtil_Factory();
  }
}
