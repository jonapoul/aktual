package actual.app.di

import actual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import actual.app.di.BuildConfig as AgpBuildConfig

@BindingContainer
@ContributesTo(AppScope::class)
object AndroidBuildConfigContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun buildConfig(): BuildConfig = buildConfig(isDebug = AgpBuildConfig.DEBUG)
}
