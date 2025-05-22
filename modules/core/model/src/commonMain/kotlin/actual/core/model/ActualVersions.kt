package actual.core.model

import alakazam.kotlin.core.BuildConfig
import alakazam.kotlin.core.StateHolder
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.flow.update

@Poko class ActualVersions(
  val app: String,
  val server: String?,
) {
  override fun toString() = "App: ${app.optionalPrefixed()} | Server: ${server.optionalPrefixed()}"

  private fun String?.optionalPrefixed(): String = when {
    this == null -> "Unknown"
    this.startsWith(prefix = "v") -> this
    else -> "v$this"
  }

  companion object {
    val Dummy = ActualVersions(app = "1.2.3", server = "24.3.0")
  }
}

class ActualVersionsStateHolder(
  private val buildConfig: BuildConfig,
) : StateHolder<ActualVersions>(from(buildConfig, server = null)) {
  fun set(serverVersion: String?) = update { from(buildConfig, serverVersion) }

  private companion object {
    fun from(build: BuildConfig, server: String?) = ActualVersions(app = build.versionName, server = server)
  }
}
