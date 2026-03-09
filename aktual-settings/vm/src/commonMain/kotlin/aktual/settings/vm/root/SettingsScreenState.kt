package aktual.settings.vm.root

import aktual.budget.model.NumberFormat
import aktual.settings.vm.BooleanPreference
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class SettingsScreenState(
  val showBottomBar: BooleanPreference,
  val numberFormat: NumberFormatPreference,
  val hideFraction: BooleanPreference,
)

@Immutable
data class NumberFormatPreference(val selected: NumberFormat, val enabled: Boolean = true) {
  val values = NumberFormat.entries.toImmutableList()
}
