package aktual.prefs.ui.inspect

import androidx.compose.runtime.Immutable

@Immutable internal sealed interface InspectThemeAction

internal data object NavBack : InspectThemeAction

internal data object OpenRepo : InspectThemeAction

internal data object Retry : InspectThemeAction

@Immutable
internal fun interface InspectThemeActionHandler {
  operator fun invoke(action: InspectThemeAction)
}
