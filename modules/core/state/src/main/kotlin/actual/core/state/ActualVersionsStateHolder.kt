package actual.core.state

import actual.core.model.ActualVersions
import alakazam.android.core.IBuildConfig
import alakazam.kotlin.core.StateHolder
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActualVersionsStateHolder @Inject constructor(
  private val buildConfig: IBuildConfig,
) : StateHolder<ActualVersions>(ActualVersions(app = buildConfig.versionName, server = null)) {
  fun set(serverVersion: String?) {
    val versions = ActualVersions(app = buildConfig.versionName, server = serverVersion)
    update { versions }
  }

  fun empty(): ActualVersions = ActualVersions(buildConfig.versionName, server = null)
}
