package aktual.core.logging

import aktual.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import okio.Path
import okio.Path.Companion.toPath

@ContributesBinding(AppScope::class)
class JvmLogStorage : LogStorage {
  override fun directory(): Path {
    val os = System.getProperty("os.name").lowercase()

    val path =
      if (os.contains("windows")) System.getenv("APPDATA") else System.getProperty("user.home")

    if (path == null) {
      error("Not sure where to store logs! os='$os'")
    }

    return path.toPath() / ".aktual" / "logs"
  }
}
