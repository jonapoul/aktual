package aktual.prefs.ui.theme.custom

import aktual.core.icons.AktualIcons
import aktual.core.icons.Git
import aktual.core.icons.material.Badge
import aktual.core.icons.material.DarkMode
import aktual.core.icons.material.LightMode
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ThemeRoutine
import aktual.core.l10n.Strings
import aktual.core.ui.BottomSheetIcon
import aktual.core.ui.ListBottomSheet
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.SetModeFilter
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.SetSorting
import aktual.prefs.vm.theme.custom.ThemeFilter
import aktual.prefs.vm.theme.custom.ThemeSorting
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.collections.immutable.toImmutableList

@Immutable internal sealed interface BottomSheet

@Immutable data class ThemeSortingBottomSheet(val value: ThemeSorting) : BottomSheet

@Immutable data class ThemeFilterBottomSheet(val value: ThemeFilter) : BottomSheet

@Composable
internal fun ThemeSortingBottomSheet(
  value: ThemeSorting,
  onAction: (CustomThemeSettingsAction) -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier,
) {
  ListBottomSheet(
    modifier = modifier,
    value = value,
    options = ThemeSorting.entries.toImmutableList(),
    onDismiss = { onAction(DismissBottomSheet) },
    onSelect = { onAction(SetSorting(it)) },
    string = { it.string() },
    leadingContent = { BottomSheetIcon(it.icon()) },
    sheetState = sheetState,
    key = { it.ordinal },
  )
}

@Composable
internal fun ThemeFilterBottomSheet(
  value: ThemeFilter,
  onAction: (CustomThemeSettingsAction) -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier,
) {
  ListBottomSheet(
    modifier = modifier,
    value = value,
    options = ThemeFilter.entries.toImmutableList(),
    onDismiss = { onAction(DismissBottomSheet) },
    onSelect = { onAction(SetModeFilter(it)) },
    string = { it.string() },
    leadingContent = { BottomSheetIcon(it.icon()) },
    sheetState = sheetState,
    key = { it.ordinal },
  )
}

@Composable
private fun ThemeSorting.string(): String =
  when (this) {
    ThemeSorting.ByName -> Strings.settingsThemeSortByName
    ThemeSorting.ByRepo -> Strings.settingsThemeSortByRepo
  }

@Stable
private fun ThemeSorting.icon(): ImageVector =
  when (this) {
    ThemeSorting.ByName -> MaterialIcons.Badge
    ThemeSorting.ByRepo -> AktualIcons.Git
  }

@Composable
private fun ThemeFilter.string(): String =
  when (this) {
    ThemeFilter.All -> Strings.settingsThemeFilterAll
    ThemeFilter.Light -> Strings.settingsThemeFilterLight
    ThemeFilter.Dark -> Strings.settingsThemeFilterDark
  }

@Stable
private fun ThemeFilter.icon(): ImageVector =
  when (this) {
    ThemeFilter.All -> MaterialIcons.ThemeRoutine
    ThemeFilter.Light -> MaterialIcons.LightMode
    ThemeFilter.Dark -> MaterialIcons.DarkMode
  }
