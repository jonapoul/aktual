package aktual.core.logging

import aktual.di.AppScope
import android.content.Context
import dev.zacsweers.metro.ContributesBinding
import okio.Path
import okio.Path.Companion.toOkioPath

@ContributesBinding(AppScope::class)
class AndroidLogStorage(private val context: Context) : LogStorage {
  override fun directory(): Path = context.filesDir.toOkioPath() / ".." / "logs"
}
