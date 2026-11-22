package aktual.core.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import java.io.FileNotFoundException
import java.io.InputStream
import kotlinx.collections.immutable.ImmutableList

@Composable
actual fun rememberFilePicker(
  mimeTypes: ImmutableList<String>,
  extension: String,
  title: String,
  onPick: (open: () -> InputStream) -> Unit,
): FilePicker {
  val resolver = LocalContext.current.contentResolver
  val currentOnPick by rememberUpdatedState(onPick)
  val launcher =
    rememberLauncherForActivityResult(OpenDocument()) { uri ->
      if (uri != null) {
        currentOnPick { resolver.openInputStream(uri) ?: throw FileNotFoundException("$uri") }
      }
    }
  return remember(launcher, mimeTypes) { FilePicker { launcher.launch(mimeTypes.toTypedArray()) } }
}
