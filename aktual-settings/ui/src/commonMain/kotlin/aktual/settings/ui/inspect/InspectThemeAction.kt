package aktual.settings.ui.inspect

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface InspectThemeAction {
  data object NavBack : InspectThemeAction

  data object Retry : InspectThemeAction
}
