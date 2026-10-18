package aktual.about.ui.logs

import aktual.about.vm.logs.LogExport
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import logcat.logcat

@Composable
internal actual fun rememberLogExportHandler(): LogExportHandler {
  val scope = rememberCoroutineScope()
  val title = Strings.logsExportSave
  return remember(scope, title) { DesktopLogExportHandler(scope, title) }
}

@Suppress("InjectDispatcher")
private class DesktopLogExportHandler(
  private val scope: CoroutineScope,
  private val title: String,
) : LogExportHandler {
  override val canShare = false

  override fun share(export: LogExport) = save(export)

  override fun save(export: LogExport) {
    val dialog = FileDialog(null as Frame?, title, FileDialog.SAVE)
    dialog.file = export.fileName
    dialog.isVisible = true

    val name = dialog.file ?: return
    val file = File(dialog.directory, name)
    scope.launch(Dispatchers.IO) {
      file.writeText(export.text)
      logcat.i { "Saved logs to $file" }
    }
  }
}
