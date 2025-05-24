package actual.logging

import okio.Path
import okio.Path.Companion.toPath

class JvmLogStorage(private val overrideUserPath: Path? = null) : LogStorage {
  override fun directory(): Path {
    if (overrideUserPath != null) {
      return overrideUserPath
    }

    val os = System.getProperty("os.name").lowercase()

    val path = when {
      os.contains("windows") -> System.getenv("APPDATA")
      else -> System.getProperty("user.home")
    }

    return path.toPath().resolve("actual/logs")
  }
}
