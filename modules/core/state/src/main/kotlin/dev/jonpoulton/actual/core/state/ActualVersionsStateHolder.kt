package dev.jonpoulton.actual.core.state

import alakazam.android.core.IBuildConfig
import alakazam.kotlin.core.StateHolder
import dev.jonpoulton.actual.core.model.ActualVersions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActualVersionsStateHolder @Inject constructor(
  private val buildConfig: IBuildConfig,
) : StateHolder<ActualVersions>(initialState = ActualVersions(app = buildConfig.versionName, server = null)) {
  fun set(serverVersion: String?) {
    val versions = ActualVersions(app = buildConfig.versionName, server = serverVersion)
    set(versions)
  }
}
