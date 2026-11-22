package aktual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import java.io.InputStream

/** Lets the user pick a file to read. [onPicked] gets an opener, which blocks so call it on IO. */
@Stable
fun interface FilePicker {
  fun pick()
}

@Composable
expect fun rememberFilePicker(
  mimeTypes: List<String>,
  extension: String,
  title: String,
  onPicked: (open: () -> InputStream) -> Unit,
): FilePicker
