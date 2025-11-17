/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.android

import aktual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import aktual.app.android.BuildConfig as AppBuildConfig

@BindingContainer
@ContributesTo(AppScope::class)
object AndroidBuildConfigContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun buildConfig(): BuildConfig = aktual.app.di.buildConfig(isDebug = AppBuildConfig.DEBUG)
}
