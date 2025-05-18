package actual.about.info.ui

import actual.core.di.AlakazamModule
import actual.core.di.ContextModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module(
  includes = [
    AlakazamModule::class,
    ContextModule::class,
  ]
)
interface TestAboutModule
