package aktual.core.ui

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

actual fun plainTextClipEntry(text: String): ClipEntry =
  ClipEntry(ClipData.newPlainText(null, text))
