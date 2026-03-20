package aktual.settings.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable data class BooleanPreference(val value: Boolean, val enabled: Boolean = true)

@Immutable
data class ListPreference<T : Any>(
  val selected: T,
  val values: ImmutableList<T>,
  val enabled: Boolean = true,
)
