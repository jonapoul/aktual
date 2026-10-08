package aktual.prefs.ui.inspect

import aktual.prefs.vm.inspect.PropertySorting
import androidx.compose.runtime.Immutable

internal sealed interface InspectThemeAction

internal data object NavBack : InspectThemeAction

internal data object OpenSearch : InspectThemeAction

internal data object OpenRepo : InspectThemeAction

internal data object Retry : InspectThemeAction

internal data object ShowSortSheet : InspectThemeAction

internal data object DismissSortSheet : InspectThemeAction

@JvmInline internal value class SetSorting(val sorting: PropertySorting) : InspectThemeAction

@Immutable
internal fun interface InspectThemeActionHandler {
  operator fun invoke(action: InspectThemeAction)
}
