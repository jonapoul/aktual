package aktual.app.desktop

import aktual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)
object DesktopBuildConfigContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun buildConfig(): BuildConfig = aktual.app.di.buildConfig(isDebug = false)
}
