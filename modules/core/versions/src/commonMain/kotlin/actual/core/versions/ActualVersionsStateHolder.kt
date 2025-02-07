package actual.core.versions

import actual.core.config.BuildConfig
import alakazam.kotlin.core.StateHolder
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActualVersionsStateHolder @Inject constructor(
  private val buildConfig: BuildConfig,
) : StateHolder<ActualVersions>(ActualVersions(app = buildConfig.versionName, server = null)) {
  fun set(serverVersion: String?) {
    val versions = ActualVersions(app = buildConfig.versionName, server = serverVersion)
    update { versions }
  }

  fun empty(): ActualVersions = ActualVersions(buildConfig.versionName, server = null)
}
