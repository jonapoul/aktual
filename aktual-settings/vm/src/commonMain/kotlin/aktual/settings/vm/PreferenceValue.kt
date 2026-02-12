package aktual.settings.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PreferenceValue {
  data class Theme(val config: ThemeConfig) : PreferenceValue

  data class ShowBottomBar(val show: Boolean) : PreferenceValue
}
