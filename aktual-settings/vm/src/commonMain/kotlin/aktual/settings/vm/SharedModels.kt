package aktual.settings.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class BooleanPreference(
  val value: Boolean,
  val onValueChange: (Boolean) -> Unit,
  val enabled: Boolean = true,
)

@Immutable
data class ListPreference<T>(
  val selected: T?,
  val values: ImmutableList<T>,
  val onValueChange: (T) -> Unit,
  val enabled: Boolean = true,
)
