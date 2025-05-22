package actual.settings.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import dev.drewhamilton.poko.Poko

@Immutable
internal sealed interface Clickability

@Immutable
internal data object NotClickable : Clickability

@Immutable
@Poko internal class Clickable(
  val enabled: Boolean,
  val onClick: () -> Unit,
) : Clickability

@Stable
internal fun Clickable(onClick: () -> Unit) = Clickable(enabled = true, onClick)
