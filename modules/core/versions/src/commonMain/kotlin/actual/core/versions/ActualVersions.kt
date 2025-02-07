package actual.core.versions

import androidx.compose.runtime.Immutable

@Immutable
data class ActualVersions(
  val app: String,
  val server: String?,
) {
  override fun toString() = "App: ${app.optionalPrefixed()} | Server: ${server.optionalPrefixed()}"

  private fun String?.optionalPrefixed(): String = when {
    this == null -> "Unknown"
    this.startsWith(prefix = "v") -> this
    else -> "v$this"
  }
}
