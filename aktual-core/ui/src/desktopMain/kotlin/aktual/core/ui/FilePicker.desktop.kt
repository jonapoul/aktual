package aktual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.InputStream

@Composable
actual fun rememberFilePicker(
  mimeTypes: List<String>,
  extension: String,
  title: String,
  onPicked: (open: () -> InputStream) -> Unit,
): FilePicker {
  val currentOnPicked by rememberUpdatedState(onPicked)
  return remember(extension, title) {
    FilePicker {
      val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
      dialog.setFilenameFilter { _, name -> name.endsWith(".$extension", ignoreCase = true) }
      // Windows ignores the filter but takes a wildcard name
      dialog.file = "*.$extension"
      dialog.isVisible = true

      val name = dialog.file ?: return@FilePicker
      val file = File(dialog.directory, name)
      currentOnPicked { file.inputStream() }
    }
  }
}
