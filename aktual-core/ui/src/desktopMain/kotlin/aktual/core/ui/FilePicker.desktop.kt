package aktual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.InputStream
import kotlinx.collections.immutable.ImmutableList

@Composable
actual fun rememberFilePicker(
  mimeTypes: ImmutableList<String>,
  extension: String,
  title: String,
  onPick: (open: () -> InputStream) -> Unit,
): FilePicker {
  val currentOnPick by rememberUpdatedState(onPick)
  return remember(extension, title) {
    FilePicker {
      val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
      dialog.setFilenameFilter { _, name -> name.endsWith(".$extension", ignoreCase = true) }
      // Windows ignores the filter but takes a wildcard name
      dialog.file = "*.$extension"
      dialog.isVisible = true

      val name = dialog.file ?: return@FilePicker
      val file = File(dialog.directory, name)
      currentOnPick { file.inputStream() }
    }
  }
}
