package aktual.core.logging

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

    if (path == null) {
      error("Not sure where to store logs! os='$os'")
    }

    return path.toPath() / ".aktual" / "logs"
  }
}
