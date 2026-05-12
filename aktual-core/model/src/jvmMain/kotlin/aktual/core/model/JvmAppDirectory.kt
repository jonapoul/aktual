package aktual.core.model

import aktual.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.io.File
import okio.Path
import okio.Path.Companion.toOkioPath

@ContributesBinding(AppScope::class)
object JvmAppDirectory : AppDirectory {
  override fun get(): Path = File(System.getProperty("user.home")).toOkioPath().resolve(".aktual")
}
