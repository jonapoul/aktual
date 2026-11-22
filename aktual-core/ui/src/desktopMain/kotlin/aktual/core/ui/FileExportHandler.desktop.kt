package aktual.core.ui

import aktual.log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
actual fun rememberFileExportHandler(mimeType: String, saveTitle: String): FileExportHandler {
  val scope = rememberCoroutineScope()
  return remember(scope, saveTitle) { DesktopFileExportHandler(scope, saveTitle) }
}

@Suppress("InjectDispatcher")
private class DesktopFileExportHandler(
  private val scope: CoroutineScope,
  private val title: String,
) : FileExportHandler {
  override val canShare = false

  override fun share(file: ExportFile) = save(file)

  override fun save(file: ExportFile) {
    val dialog = FileDialog(null as Frame?, title, FileDialog.SAVE)
    dialog.file = file.fileName
    dialog.isVisible = true

    val name = dialog.file
    if (name == null) {
      file.onDone()
      return
    }
    val target = File(dialog.directory, name)
    scope.launch(Dispatchers.IO) {
      try {
        target.outputStream().use(file.write)
        log.i { "Saved ${file.fileName} to $target" }
      } catch (e: IOException) {
        log.e(e) { "Failed saving ${file.fileName} to $target" }
      } finally {
        file.onDone()
      }
    }
  }
}
