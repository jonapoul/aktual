package aktual.settings.ui

import androidx.compose.runtime.Immutable

@Immutable
fun interface SettingsNavigator {
  fun back(): Boolean
}
