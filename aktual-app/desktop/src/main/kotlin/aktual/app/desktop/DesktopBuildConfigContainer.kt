package aktual.app.desktop

import aktual.core.model.BuildConfig
import aktual.di.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)
object DesktopBuildConfigContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun buildConfig(): BuildConfig = aktual.app.di.buildConfig(isDebug = !isRelease())

  // Rewritten to return true by proguard-rules.pro, which only applies to release builds
  @JvmStatic private fun isRelease(): Boolean = false
}
