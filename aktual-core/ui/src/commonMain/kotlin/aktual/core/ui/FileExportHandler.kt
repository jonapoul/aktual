package aktual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import java.io.OutputStream

/** Saves a file somewhere the user picks, or shares it to another app where the platform can. */
@Stable
interface FileExportHandler {
  val canShare: Boolean

  fun share(file: ExportFile)

  fun save(file: ExportFile)
}

/** [write] runs on an IO thread once the destination is known. */
@Immutable class ExportFile(val fileName: String, val write: (OutputStream) -> Unit)

@Composable
expect fun rememberFileExportHandler(mimeType: String, saveTitle: String): FileExportHandler
