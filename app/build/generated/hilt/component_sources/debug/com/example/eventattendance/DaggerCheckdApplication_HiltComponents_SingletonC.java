package com.example.eventattendance;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.eventattendance.data.remote.datasource.AuthRemoteDataSource;
import com.example.eventattendance.data.remote.datasource.EventRemoteDataSource;
import com.example.eventattendance.data.remote.datasource.RpcRemoteDataSource;
import com.example.eventattendance.data.repository.AttendanceMatrixRepositoryImpl;
import com.example.eventattendance.data.repository.AttendanceRepositoryImpl;
import com.example.eventattendance.data.repository.AuthRepositoryImpl;
import com.example.eventattendance.data.repository.DirectoryRepositoryImpl;
import com.example.eventattendance.data.repository.EventRepositoryImpl;
import com.example.eventattendance.data.repository.ParticipantCacheRepositoryImpl;
import com.example.eventattendance.data.repository.SyncRepositoryImpl;
import com.example.eventattendance.di.AppModule_ProvideIoDispatcherFactory;
import com.example.eventattendance.di.AppModule_ProvidePreferencesDataStoreFactory;
import com.example.eventattendance.di.NetworkModule_ProvideSupabaseClientFactory;
import com.example.eventattendance.domain.usecase.LogoutUseCase;
import com.example.eventattendance.domain.usecase.LookupParticipantUseCase;
import com.example.eventattendance.domain.usecase.ManageEventsUseCase;
import com.example.eventattendance.domain.usecase.MarkAttendanceUseCase;
import com.example.eventattendance.domain.usecase.StaffLoginUseCase;
import com.example.eventattendance.domain.usecase.SyncDataUseCase;
import com.example.eventattendance.presentation.admin.AdminViewModel;
import com.example.eventattendance.presentation.admin.AdminViewModel_HiltModules;
import com.example.eventattendance.presentation.admin.AdminViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.example.eventattendance.presentation.admin.AdminViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.example.eventattendance.presentation.admin.ParticipantDetailViewModel;
import com.example.eventattendance.presentation.admin.ParticipantDetailViewModel_HiltModules;
import com.example.eventattendance.presentation.admin.ParticipantDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.example.eventattendance.presentation.admin.ParticipantDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.example.eventattendance.presentation.admin.SpocDetailViewModel;
import com.example.eventattendance.presentation.admin.SpocDetailViewModel_HiltModules;
import com.example.eventattendance.presentation.admin.SpocDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.example.eventattendance.presentation.admin.SpocDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.example.eventattendance.presentation.authentication.StaffAuthViewModel;
import com.example.eventattendance.presentation.authentication.StaffAuthViewModel_HiltModules;
import com.example.eventattendance.presentation.authentication.StaffAuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.example.eventattendance.presentation.authentication.StaffAuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.example.eventattendance.presentation.eventdetails.EventDetailViewModel;
import com.example.eventattendance.presentation.eventdetails.EventDetailViewModel_HiltModules;
import com.example.eventattendance.presentation.eventdetails.EventDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.example.eventattendance.presentation.eventdetails.EventDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.example.eventattendance.presentation.home.HomeViewModel;
import com.example.eventattendance.presentation.home.HomeViewModel_HiltModules;
import com.example.eventattendance.presentation.home.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.example.eventattendance.presentation.home.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.example.eventattendance.presentation.scanner.ScannerViewModel;
import com.example.eventattendance.presentation.scanner.ScannerViewModel_HiltModules;
import com.example.eventattendance.presentation.scanner.ScannerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.example.eventattendance.presentation.scanner.ScannerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.example.eventattendance.presentation.settings.SettingsViewModel;
import com.example.eventattendance.presentation.settings.SettingsViewModel_HiltModules;
import com.example.eventattendance.presentation.settings.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.example.eventattendance.presentation.settings.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.example.eventattendance.presentation.spoc.SpocDashboardViewModel;
import com.example.eventattendance.presentation.spoc.SpocDashboardViewModel_HiltModules;
import com.example.eventattendance.presentation.spoc.SpocDashboardViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.example.eventattendance.presentation.spoc.SpocDashboardViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.example.eventattendance.sync.NetworkConnectivityObserver;
import com.example.eventattendance.sync.SyncManager;
import com.example.eventattendance.util.AudioFeedbackUtil;
import com.example.eventattendance.util.HapticFeedbackUtil;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import io.github.jan.supabase.SupabaseClient;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerCheckdApplication_HiltComponents_SingletonC {
  private DaggerCheckdApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public CheckdApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements CheckdApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public CheckdApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements CheckdApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public CheckdApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements CheckdApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public CheckdApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements CheckdApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CheckdApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements CheckdApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CheckdApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements CheckdApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public CheckdApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements CheckdApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public CheckdApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends CheckdApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends CheckdApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends CheckdApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends CheckdApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    Map keySetMapOfClassOfObjectAndBooleanBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, Boolean>newMapBuilder(10);
      mapBuilder.put(AdminViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AdminViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(EventDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, EventDetailViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(MainViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MainViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ParticipantDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ParticipantDetailViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ScannerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ScannerViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(SpocDashboardViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SpocDashboardViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(SpocDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SpocDetailViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(StaffAuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, StaffAuthViewModel_HiltModules.KeyModule.provide());
      return mapBuilder.build();
    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(keySetMapOfClassOfObjectAndBooleanBuilder());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends CheckdApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<AdminViewModel> adminViewModelProvider;

    Provider<EventDetailViewModel> eventDetailViewModelProvider;

    Provider<HomeViewModel> homeViewModelProvider;

    Provider<MainViewModel> mainViewModelProvider;

    Provider<ParticipantDetailViewModel> participantDetailViewModelProvider;

    Provider<ScannerViewModel> scannerViewModelProvider;

    Provider<SettingsViewModel> settingsViewModelProvider;

    Provider<SpocDashboardViewModel> spocDashboardViewModelProvider;

    Provider<SpocDetailViewModel> spocDetailViewModelProvider;

    Provider<StaffAuthViewModel> staffAuthViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    ManageEventsUseCase manageEventsUseCase() {
      return new ManageEventsUseCase(singletonCImpl.eventRepositoryImplProvider.get());
    }

    LogoutUseCase logoutUseCase() {
      return new LogoutUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    SyncDataUseCase syncDataUseCase() {
      return new SyncDataUseCase(singletonCImpl.syncRepositoryImplProvider.get());
    }

    LookupParticipantUseCase lookupParticipantUseCase() {
      return new LookupParticipantUseCase(singletonCImpl.participantCacheRepositoryImplProvider.get());
    }

    MarkAttendanceUseCase markAttendanceUseCase() {
      return new MarkAttendanceUseCase(singletonCImpl.attendanceRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get());
    }

    StaffLoginUseCase staffLoginUseCase() {
      return new StaffLoginUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    Map hiltViewModelMapMapOfClassOfObjectAndProviderOfViewModelBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(10);
      mapBuilder.put(AdminViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (adminViewModelProvider)));
      mapBuilder.put(EventDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (eventDetailViewModelProvider)));
      mapBuilder.put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (homeViewModelProvider)));
      mapBuilder.put(MainViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (mainViewModelProvider)));
      mapBuilder.put(ParticipantDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (participantDetailViewModelProvider)));
      mapBuilder.put(ScannerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (scannerViewModelProvider)));
      mapBuilder.put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (settingsViewModelProvider)));
      mapBuilder.put(SpocDashboardViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (spocDashboardViewModelProvider)));
      mapBuilder.put(SpocDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (spocDetailViewModelProvider)));
      mapBuilder.put(StaffAuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (staffAuthViewModelProvider)));
      return mapBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.adminViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.eventDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.mainViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.participantDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.scannerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.spocDashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.spocDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.staffAuthViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(hiltViewModelMapMapOfClassOfObjectAndProviderOfViewModelBuilder());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.example.eventattendance.presentation.admin.AdminViewModel
          return (T) new AdminViewModel(singletonCImpl.attendanceMatrixRepositoryImplProvider.get(), singletonCImpl.participantCacheRepositoryImplProvider.get(), singletonCImpl.directoryRepositoryImplProvider.get(), singletonCImpl.attendanceRepositoryImplProvider.get(), singletonCImpl.eventRepositoryImplProvider.get(), viewModelCImpl.manageEventsUseCase(), singletonCImpl.authRepositoryImplProvider.get(), viewModelCImpl.logoutUseCase());

          case 1: // com.example.eventattendance.presentation.eventdetails.EventDetailViewModel
          return (T) new EventDetailViewModel(singletonCImpl.attendanceRepositoryImplProvider.get(), singletonCImpl.participantCacheRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get(), singletonCImpl.attendanceMatrixRepositoryImplProvider.get(), viewModelCImpl.savedStateHandle);

          case 2: // com.example.eventattendance.presentation.home.HomeViewModel
          return (T) new HomeViewModel(singletonCImpl.attendanceRepositoryImplProvider.get(), viewModelCImpl.syncDataUseCase());

          case 3: // com.example.eventattendance.MainViewModel
          return (T) new MainViewModel(singletonCImpl.authRepositoryImplProvider.get(), singletonCImpl.participantCacheRepositoryImplProvider.get());

          case 4: // com.example.eventattendance.presentation.admin.ParticipantDetailViewModel
          return (T) new ParticipantDetailViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.participantCacheRepositoryImplProvider.get(), singletonCImpl.attendanceMatrixRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get());

          case 5: // com.example.eventattendance.presentation.scanner.ScannerViewModel
          return (T) new ScannerViewModel(viewModelCImpl.lookupParticipantUseCase(), viewModelCImpl.markAttendanceUseCase(), singletonCImpl.attendanceRepositoryImplProvider.get(), singletonCImpl.attendanceMatrixRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get(), singletonCImpl.hapticFeedbackUtilProvider.get(), singletonCImpl.audioFeedbackUtilProvider.get(), viewModelCImpl.savedStateHandle);

          case 6: // com.example.eventattendance.presentation.settings.SettingsViewModel
          return (T) new SettingsViewModel(singletonCImpl.authRepositoryImplProvider.get(), viewModelCImpl.logoutUseCase(), singletonCImpl.participantCacheRepositoryImplProvider.get());

          case 7: // com.example.eventattendance.presentation.spoc.SpocDashboardViewModel
          return (T) new SpocDashboardViewModel(singletonCImpl.authRepositoryImplProvider.get(), singletonCImpl.directoryRepositoryImplProvider.get(), singletonCImpl.participantCacheRepositoryImplProvider.get(), singletonCImpl.attendanceMatrixRepositoryImplProvider.get(), viewModelCImpl.logoutUseCase());

          case 8: // com.example.eventattendance.presentation.admin.SpocDetailViewModel
          return (T) new SpocDetailViewModel(singletonCImpl.directoryRepositoryImplProvider.get(), singletonCImpl.participantCacheRepositoryImplProvider.get(), singletonCImpl.attendanceMatrixRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get(), viewModelCImpl.savedStateHandle);

          case 9: // com.example.eventattendance.presentation.authentication.StaffAuthViewModel
          return (T) new StaffAuthViewModel(viewModelCImpl.staffLoginUseCase());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends CheckdApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends CheckdApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends CheckdApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<SupabaseClient> provideSupabaseClientProvider;

    Provider<RpcRemoteDataSource> rpcRemoteDataSourceProvider;

    Provider<AttendanceMatrixRepositoryImpl> attendanceMatrixRepositoryImplProvider;

    Provider<ParticipantCacheRepositoryImpl> participantCacheRepositoryImplProvider;

    Provider<DirectoryRepositoryImpl> directoryRepositoryImplProvider;

    Provider<AttendanceRepositoryImpl> attendanceRepositoryImplProvider;

    Provider<EventRemoteDataSource> eventRemoteDataSourceProvider;

    Provider<EventRepositoryImpl> eventRepositoryImplProvider;

    Provider<AuthRemoteDataSource> authRemoteDataSourceProvider;

    Provider<DataStore<Preferences>> providePreferencesDataStoreProvider;

    Provider<NetworkConnectivityObserver> networkConnectivityObserverProvider;

    Provider<AuthRepositoryImpl> authRepositoryImplProvider;

    Provider<SyncManager> syncManagerProvider;

    Provider<SyncRepositoryImpl> syncRepositoryImplProvider;

    Provider<HapticFeedbackUtil> hapticFeedbackUtilProvider;

    Provider<AudioFeedbackUtil> audioFeedbackUtilProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideSupabaseClientProvider = DoubleCheck.provider(new SwitchingProvider<SupabaseClient>(singletonCImpl, 2));
      this.rpcRemoteDataSourceProvider = DoubleCheck.provider(new SwitchingProvider<RpcRemoteDataSource>(singletonCImpl, 1));
      this.attendanceMatrixRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AttendanceMatrixRepositoryImpl>(singletonCImpl, 0));
      this.participantCacheRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ParticipantCacheRepositoryImpl>(singletonCImpl, 3));
      this.directoryRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<DirectoryRepositoryImpl>(singletonCImpl, 4));
      this.attendanceRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AttendanceRepositoryImpl>(singletonCImpl, 5));
      this.eventRemoteDataSourceProvider = DoubleCheck.provider(new SwitchingProvider<EventRemoteDataSource>(singletonCImpl, 7));
      this.eventRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<EventRepositoryImpl>(singletonCImpl, 6));
      this.authRemoteDataSourceProvider = DoubleCheck.provider(new SwitchingProvider<AuthRemoteDataSource>(singletonCImpl, 9));
      this.providePreferencesDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<DataStore<Preferences>>(singletonCImpl, 10));
      this.networkConnectivityObserverProvider = DoubleCheck.provider(new SwitchingProvider<NetworkConnectivityObserver>(singletonCImpl, 11));
      this.authRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepositoryImpl>(singletonCImpl, 8));
      this.syncManagerProvider = DoubleCheck.provider(new SwitchingProvider<SyncManager>(singletonCImpl, 13));
      this.syncRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<SyncRepositoryImpl>(singletonCImpl, 12));
      this.hapticFeedbackUtilProvider = DoubleCheck.provider(new SwitchingProvider<HapticFeedbackUtil>(singletonCImpl, 14));
      this.audioFeedbackUtilProvider = DoubleCheck.provider(new SwitchingProvider<AudioFeedbackUtil>(singletonCImpl, 15));
    }

    @Override
    public void injectCheckdApplication(CheckdApplication checkdApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.example.eventattendance.data.repository.AttendanceMatrixRepositoryImpl
          return (T) new AttendanceMatrixRepositoryImpl(singletonCImpl.rpcRemoteDataSourceProvider.get(), AppModule_ProvideIoDispatcherFactory.provideIoDispatcher());

          case 1: // com.example.eventattendance.data.remote.datasource.RpcRemoteDataSource
          return (T) new RpcRemoteDataSource(singletonCImpl.provideSupabaseClientProvider.get());

          case 2: // io.github.jan.supabase.SupabaseClient
          return (T) NetworkModule_ProvideSupabaseClientFactory.provideSupabaseClient();

          case 3: // com.example.eventattendance.data.repository.ParticipantCacheRepositoryImpl
          return (T) new ParticipantCacheRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.rpcRemoteDataSourceProvider.get(), AppModule_ProvideIoDispatcherFactory.provideIoDispatcher());

          case 4: // com.example.eventattendance.data.repository.DirectoryRepositoryImpl
          return (T) new DirectoryRepositoryImpl(singletonCImpl.participantCacheRepositoryImplProvider.get(), singletonCImpl.provideSupabaseClientProvider.get(), AppModule_ProvideIoDispatcherFactory.provideIoDispatcher());

          case 5: // com.example.eventattendance.data.repository.AttendanceRepositoryImpl
          return (T) new AttendanceRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.rpcRemoteDataSourceProvider.get(), AppModule_ProvideIoDispatcherFactory.provideIoDispatcher());

          case 6: // com.example.eventattendance.data.repository.EventRepositoryImpl
          return (T) new EventRepositoryImpl(singletonCImpl.eventRemoteDataSourceProvider.get(), singletonCImpl.attendanceRepositoryImplProvider.get());

          case 7: // com.example.eventattendance.data.remote.datasource.EventRemoteDataSource
          return (T) new EventRemoteDataSource(singletonCImpl.provideSupabaseClientProvider.get());

          case 8: // com.example.eventattendance.data.repository.AuthRepositoryImpl
          return (T) new AuthRepositoryImpl(singletonCImpl.authRemoteDataSourceProvider.get(), singletonCImpl.providePreferencesDataStoreProvider.get(), singletonCImpl.networkConnectivityObserverProvider.get(), AppModule_ProvideIoDispatcherFactory.provideIoDispatcher());

          case 9: // com.example.eventattendance.data.remote.datasource.AuthRemoteDataSource
          return (T) new AuthRemoteDataSource(singletonCImpl.provideSupabaseClientProvider.get());

          case 10: // androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
          return (T) AppModule_ProvidePreferencesDataStoreFactory.providePreferencesDataStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 11: // com.example.eventattendance.sync.NetworkConnectivityObserver
          return (T) new NetworkConnectivityObserver(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 12: // com.example.eventattendance.data.repository.SyncRepositoryImpl
          return (T) new SyncRepositoryImpl(singletonCImpl.syncManagerProvider.get(), singletonCImpl.networkConnectivityObserverProvider.get());

          case 13: // com.example.eventattendance.sync.SyncManager
          return (T) new SyncManager(singletonCImpl.networkConnectivityObserverProvider.get());

          case 14: // com.example.eventattendance.util.HapticFeedbackUtil
          return (T) new HapticFeedbackUtil(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 15: // com.example.eventattendance.util.AudioFeedbackUtil
          return (T) new AudioFeedbackUtil();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
