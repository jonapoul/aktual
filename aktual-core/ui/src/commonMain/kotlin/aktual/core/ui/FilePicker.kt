package aktual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import java.io.InputStream
import kotlinx.collections.immutable.ImmutableList

/** Lets the user pick a file to read. [onPick] gets an opener, which blocks so call it on IO. */
@Stable
fun interface FilePicker {
  fun pick()
}

@Composable
expect fun rememberFilePicker(
  mimeTypes: ImmutableList<String>,
  extension: String,
  title: String,
  onPick: (open: () -> InputStream) -> Unit,
): FilePicker
