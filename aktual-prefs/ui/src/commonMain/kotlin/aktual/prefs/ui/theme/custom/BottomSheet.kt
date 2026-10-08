package aktual.prefs.ui.theme.custom

import aktual.core.icons.AktualIcons
import aktual.core.icons.Git
import aktual.core.icons.material.Badge
import aktual.core.icons.material.DarkMode
import aktual.core.icons.material.LightMode
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ThemeRoutine
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSheetIcon
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.verticalScrollWithBar
import aktual.prefs.vm.theme.custom.ThemeFilter
import aktual.prefs.vm.theme.custom.ThemeSorting
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach

@Composable
internal fun SortFilterBottomSheet(
  sorting: ThemeSorting,
  filter: ThemeFilter,
  onAction: CustomThemeSettingsActionHandler,
  sheetState: SheetState,
  modifier: Modifier = Modifier,
) {
  AktualModalBottomSheet(
    modifier = modifier,
    onDismissRequest = { onAction(DismissBottomSheet) },
    sheetState = sheetState,
  ) {
    Column(modifier = Modifier.verticalScrollWithBar()) {
      SectionHeader(Strings.settingsThemeSort)
      ThemeSorting.entries.fastForEach { option ->
        BottomSheetListItem(
          label = option.string(),
          isSelected = option == sorting,
          onClick = { onAction(SetSorting(option)) },
          leadingContent = { BottomSheetIcon(option.icon()) },
        )
      }

      VerticalSpacer(4.dp)

      SectionHeader(Strings.settingsThemeFilter)
      ThemeFilter.entries.fastForEach { option ->
        BottomSheetListItem(
          label = option.string(),
          isSelected = option == filter,
          onClick = { onAction(SetModeFilter(option)) },
          leadingContent = { BottomSheetIcon(option.icon()) },
        )
      }
    }
  }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
  Text(
    modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    text = text,
    style = typography.labelMedium,
    color = colors.pageTextSubdued,
  )
}

@Composable
private fun ThemeSorting.string(): String =
  when (this) {
    ByName -> Strings.settingsThemeSortByName
    ByRepo -> Strings.settingsThemeSortByRepo
  }

@Stable
private fun ThemeSorting.icon(): ImageVector =
  when (this) {
    ByName -> MaterialIcons.Badge
    ByRepo -> AktualIcons.Git
  }

@Composable
private fun ThemeFilter.string(): String =
  when (this) {
    All -> Strings.settingsThemeFilterAll
    Light -> Strings.settingsThemeFilterLight
    Dark -> Strings.settingsThemeFilterDark
  }

@Stable
private fun ThemeFilter.icon(): ImageVector =
  when (this) {
    All -> MaterialIcons.ThemeRoutine
    Light -> MaterialIcons.LightMode
    Dark -> MaterialIcons.DarkMode
  }
