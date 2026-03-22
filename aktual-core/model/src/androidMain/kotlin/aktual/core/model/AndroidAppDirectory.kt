package aktual.core.model

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath

@ContributesBinding(AppScope::class)
class AndroidAppDirectory(private val context: Context, private val fileSystem: FileSystem) :
  AppDirectory {
  private val path by lazy {
    val path =
      context.getDatabasePath("unused").parentFile?.parentFile?.toOkioPath()
        ?: error("No path found for ${context.getDatabasePath("unused")}")

    if (!fileSystem.exists(path)) {
      error("Expected path doesn't exist at $path")
    }

    path
  }

  override fun get(): Path = path
}
