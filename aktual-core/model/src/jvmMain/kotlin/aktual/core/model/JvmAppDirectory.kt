package aktual.core.model

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import okio.Path
import okio.Path.Companion.toPath

@ContributesBinding(AppScope::class)
class JvmAppDirectory(private val path: Path) : AppDirectory {
  @Inject constructor() : this(System.getProperty("user.home").toPath())

  override fun get(): Path = path.resolve(".aktual")
}
