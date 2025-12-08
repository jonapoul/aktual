package aktual.settings.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
internal sealed interface Clickability

@Immutable
internal data object NotClickable : Clickability

@Immutable
internal data class Clickable(
  val enabled: Boolean = true,
  val onClick: () -> Unit,
) : Clickability

@Stable
internal fun Clickable(onClick: () -> Unit) = Clickable(enabled = true, onClick)
