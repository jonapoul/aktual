package actual.android.app.di

import actual.core.di.ActualVersionsModule
import actual.core.di.AlakazamModule
import actual.core.di.ClockModule
import actual.core.di.ContextModule
import actual.core.di.CoroutineContextsModule
import actual.core.di.CoroutineScopeModule
import actual.core.di.FileSystemModule
import actual.core.di.FilesModule
import actual.core.di.LoopControllerModule
import actual.core.di.PreferencesModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module(
  includes = [
    ActualVersionsModule::class,
    AlakazamModule::class,
    ClockModule::class,
    ContextModule::class,
    CoroutineContextsModule::class,
    CoroutineScopeModule::class,
    FilesModule::class,
    FileSystemModule::class,
    LoopControllerModule::class,
    PreferencesModule::class,
  ],
)
interface CoreDiModule
