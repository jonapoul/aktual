package aktual.core.ui

import androidx.compose.ui.platform.ClipEntry

expect fun plainTextClipEntry(text: String): ClipEntry

/** The entry's text, or null if it holds none */
expect fun ClipEntry.plainText(): String?
