package aktual.app.desktop

import aktual.core.model.BuildConfig
import aktual.core.model.RELEASE_BUILD_TYPE
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
  fun buildConfig(): BuildConfig =
    aktual.app.di.buildConfig(
      isDebug = !isRelease(),
      buildType = if (isRelease()) RELEASE_BUILD_TYPE else "debug",
    )

  // Rewritten to return true by proguard-rules.pro, which only applies to release builds
  @Suppress("FunctionOnlyReturningConstant") @JvmStatic private fun isRelease(): Boolean = false
}
