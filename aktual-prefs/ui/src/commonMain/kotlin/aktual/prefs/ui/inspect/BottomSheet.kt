package aktual.prefs.ui.inspect

import aktual.core.icons.material.Badge
import aktual.core.icons.material.FormatListBulleted
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ThemeRoutine
import aktual.core.l10n.Strings
import aktual.core.ui.BottomSheetIcon
import aktual.core.ui.ListBottomSheet
import aktual.prefs.vm.inspect.PropertySorting
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun PropertySortingBottomSheet(
  value: PropertySorting,
  onAction: InspectThemeActionHandler,
  sheetState: SheetState,
  modifier: Modifier = Modifier,
) {
  ListBottomSheet(
    modifier = modifier,
    value = value,
    options = PropertySorting.entries.toImmutableList(),
    onDismiss = { onAction(DismissSortSheet) },
    onSelect = { onAction(SetSorting(it)) },
    string = { it.string() },
    leadingContent = { BottomSheetIcon(it.icon()) },
    sheetState = sheetState,
    key = { it.ordinal },
  )
}

@Composable
private fun PropertySorting.string(): String =
  when (this) {
    Default -> Strings.settingsThemeInspectSortDefault
    ByName -> Strings.settingsThemeInspectSortByName
    ByColor -> Strings.settingsThemeInspectSortByColor
  }

@Stable
private fun PropertySorting.icon(): ImageVector =
  when (this) {
    Default -> MaterialIcons.FormatListBulleted
    ByName -> MaterialIcons.Badge
    ByColor -> MaterialIcons.ThemeRoutine
  }
