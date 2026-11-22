package aktual.about.components

import aktual.about.model.LogExport
import aktual.core.l10n.Strings
import aktual.core.ui.ExportFile
import aktual.core.ui.FileExportHandler
import aktual.core.ui.plainTextClipEntry
import aktual.core.ui.rememberFileExportHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalClipboard
import kotlinx.coroutines.flow.Flow

@Composable
fun rememberLogExportHandler(): FileExportHandler =
  rememberFileExportHandler(mimeType = "text/plain", saveTitle = Strings.logsExportSave)

@Composable
fun CollectLogExports(exports: Flow<LogExport>, handler: FileExportHandler) {
  val clipboard = LocalClipboard.current
  val currentHandler by rememberUpdatedState(handler)
  LaunchedEffect(exports) {
    exports.collect { export ->
      when (export.target) {
        Share -> currentHandler.share(export.toFile())
        Save -> currentHandler.save(export.toFile())
        Copy -> clipboard.setClipEntry(plainTextClipEntry(export.text))
      }
    }
  }
}

private fun LogExport.toFile() =
  ExportFile(fileName, write = { output -> output.write(text.encodeToByteArray()) })
