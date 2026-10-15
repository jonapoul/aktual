package aktual.prefs.ui.inspect

import aktual.prefs.vm.inspect.PropertySorting
import aktual.prefs.vm.inspect.ThemeProperty
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

internal sealed interface InspectThemeAction

internal data object NavBack : InspectThemeAction

internal data object OpenSearch : InspectThemeAction

internal data object OpenRepo : InspectThemeAction

internal data object Retry : InspectThemeAction

internal data object ShowSortSheet : InspectThemeAction

internal data object DismissSortSheet : InspectThemeAction

@JvmInline internal value class SetSorting(val sorting: PropertySorting) : InspectThemeAction

internal data object ShowDuplicateDialog : InspectThemeAction

@JvmInline internal value class ConfirmDuplicate(val name: String) : InspectThemeAction

internal data object ShowDeleteDialog : InspectThemeAction

internal data object ConfirmDelete : InspectThemeAction

@JvmInline internal value class EditProperty(val property: ThemeProperty) : InspectThemeAction

internal data class SetColor(val name: String, val color: Color) : InspectThemeAction

internal data object DismissDialog : InspectThemeAction

@Immutable
internal fun interface InspectThemeActionHandler {
  operator fun invoke(action: InspectThemeAction)
}
