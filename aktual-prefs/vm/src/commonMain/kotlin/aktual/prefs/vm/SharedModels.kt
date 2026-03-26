package aktual.prefs.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class BooleanPreference(
  val value: Boolean,
  val enabled: Boolean = true,
  val onChange: (Boolean) -> Unit = {},
)

@Immutable
data class ListPreference<T : Any>(
  val value: T,
  val options: ImmutableList<T>,
  val enabled: Boolean = true,
  val onChange: (T) -> Unit = {},
)

inline fun <reified E : Enum<E>> ListPreference(
  value: E,
  enabled: Boolean = true,
  noinline onChange: (E) -> Unit = {},
): ListPreference<E> =
  ListPreference(
    value = value,
    options = enumValues<E>().toImmutableList(),
    enabled = enabled,
    onChange = onChange,
  )

@Immutable
data class SliderPreference(
  val value: Float,
  val range: ClosedFloatingPointRange<Float>,
  val enabled: Boolean = true,
  val onChange: (Float) -> Unit = {},
)
