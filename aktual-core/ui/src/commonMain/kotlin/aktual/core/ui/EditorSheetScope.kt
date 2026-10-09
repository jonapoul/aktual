package aktual.core.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Stable

/** Lets an [EditorSheet]'s content close the sheet itself, like an action that applies at once */
@Stable
class EditorSheetScope
internal constructor(
  column: ColumnScope,
  private val hide: (then: () -> Unit) -> Unit,
) : ColumnScope by column {
  fun close(then: () -> Unit = {}) = hide(then)
}
