package aktual.core.model

import alakazam.kotlin.StateHolder
import androidx.compose.runtime.Immutable
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.update

@Immutable
data class AktualVersions(val app: String, val server: ServerVersion?) {
  override fun toString(): String {
    val serverVersion =
      when (server) {
        ServerVersion.NotApplicable -> "N/A"
        else -> server?.value.optionalPrefixed()
      }
    return "App: ${app.optionalPrefixed()} | Server: $serverVersion"
  }

  companion object {
    val Dummy = AktualVersions(app = "1.2.3", server = ServerVersion("24.3.0"))

    private fun String?.optionalPrefixed(): String =
      when {
        this == null -> "Unknown"
        this.startsWith(prefix = "v") -> this
        else -> "v$this"
      }
  }
}

@Immutable
@JvmInline
value class ServerVersion(val value: String?) {
  override fun toString(): String = value ?: "N/A"

  companion object {
    val NotApplicable = ServerVersion(null)
  }
}

@Inject
@SingleIn(AppScope::class)
class AktualVersionsStateHolder(private val buildConfig: BuildConfig) :
  StateHolder<AktualVersions>(from(buildConfig, server = null)) {

  fun set(serverVersion: ServerVersion?) = update { from(buildConfig, serverVersion) }

  private companion object {
    fun from(build: BuildConfig, server: ServerVersion?) =
      AktualVersions(app = build.versionName, server = server)
  }
}
