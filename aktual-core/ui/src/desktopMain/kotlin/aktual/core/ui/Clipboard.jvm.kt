package aktual.core.ui

import aktual.core.utils.runCatchingCancellable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable

@OptIn(ExperimentalComposeUiApi::class)
actual fun plainTextClipEntry(text: String): ClipEntry = ClipEntry(StringSelection(text))

// The clipboard can change between the flavour check and the read, which then throws
@OptIn(ExperimentalComposeUiApi::class)
actual fun ClipEntry.plainText(): String? {
  val transferable = nativeClipEntry as? Transferable ?: return null
  if (!transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) return null
  return runCatchingCancellable(
    attempt = { transferable.getTransferData(DataFlavor.stringFlavor) as? String },
    onFailure = { null },
  )
}
