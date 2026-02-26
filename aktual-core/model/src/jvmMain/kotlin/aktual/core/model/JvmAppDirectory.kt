package aktual.core.model

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.io.File
import okio.Path
import okio.Path.Companion.toOkioPath

@Inject
@ContributesBinding(AppScope::class)
class JvmAppDirectory : AppDirectory {
  override fun get(): Path = File(System.getProperty("user.home")).toOkioPath().resolve(".aktual")
}
