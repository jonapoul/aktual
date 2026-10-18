package aktual.about.ui.logs

import aktual.about.vm.logs.LogExport
import aktual.core.ui.plainTextClipEntry
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalClipboard
import kotlinx.coroutines.flow.Flow

@Stable
internal interface LogExportHandler {
  val canShare: Boolean

  fun share(export: LogExport)

  fun save(export: LogExport)
}

@Composable internal expect fun rememberLogExportHandler(): LogExportHandler

@Composable
internal fun CollectLogExports(exports: Flow<LogExport>, handler: LogExportHandler) {
  val clipboard = LocalClipboard.current
  val currentHandler by rememberUpdatedState(handler)
  LaunchedEffect(exports) {
    exports.collect { export ->
      when (export.target) {
        Share -> currentHandler.share(export)
        Save -> currentHandler.save(export)
        Copy -> clipboard.setClipEntry(plainTextClipEntry(export.text))
      }
    }
  }
}
