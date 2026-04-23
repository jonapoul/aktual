package aktual.prefs.ui.theme.custom

import aktual.core.icons.AktualIcons
import aktual.core.icons.Git
import aktual.core.icons.material.Badge
import aktual.core.icons.material.Check
import aktual.core.icons.material.DarkMode
import aktual.core.icons.material.LightMode
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ThemeRoutine
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.listItem
import aktual.core.ui.scrollbar
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.SetModeFilter
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.SetSorting
import aktual.prefs.vm.theme.custom.ThemeFilter
import aktual.prefs.vm.theme.custom.ThemeSorting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Immutable internal sealed interface BottomSheet

@Immutable data class ThemeSortingBottomSheet(val value: ThemeSorting) : BottomSheet

@Immutable data class ThemeFilterBottomSheet(val value: ThemeFilter) : BottomSheet

@Composable
internal fun ThemeSortingBottomSheet(
  value: ThemeSorting,
  onAction: (CustomThemeSettingsAction) -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  ModalBottomSheet(
    modifier = modifier,
    onDismissRequest = { onAction(DismissBottomSheet) },
    sheetState = sheetState,
    containerColor = theme.modalBackground,
    contentColor = theme.pageText,
  ) {
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
      items(ThemeSorting.entries) { sorting ->
        val isSelected = sorting == value
        val label = sorting.string()
        ListItem(
          modifier =
            Modifier.clickable {
              onAction(SetSorting(sorting))
              onAction(DismissBottomSheet)
            },
          leadingContent = { BottomSheetIcon(sorting.icon(), contentDescription = label) },
          headlineContent = {
            Text(modifier = Modifier.weight(1f), text = label, textAlign = TextAlign.Center)
          },
          trailingContent = { if (isSelected) BottomSheetIcon(MaterialIcons.Check) },
          colors = theme.listItem(),
        )
      }
    }
  }
}

@Composable
internal fun ThemeFilterBottomSheet(
  value: ThemeFilter,
  onAction: (CustomThemeSettingsAction) -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  ModalBottomSheet(
    modifier = modifier,
    onDismissRequest = { onAction(DismissBottomSheet) },
    sheetState = sheetState,
    containerColor = theme.modalBackground,
    contentColor = theme.pageText,
  ) {
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
      items(ThemeFilter.entries) { filter ->
        val isSelected = filter == value
        val label = filter.string()
        ListItem(
          modifier =
            Modifier.clickable {
              onAction(SetModeFilter(filter))
              onAction(DismissBottomSheet)
            },
          leadingContent = {
            BottomSheetIcon(imageVector = filter.icon(), contentDescription = label)
          },
          headlineContent = {
            Text(modifier = Modifier.weight(1f), text = label, textAlign = TextAlign.Center)
          },
          trailingContent = {
            if (isSelected) BottomSheetIcon(MaterialIcons.Check, contentDescription = null)
          },
          colors = theme.listItem(),
        )
      }
    }
  }
}

@Composable
private fun ThemeSorting.string(): String =
  when (this) {
    ThemeSorting.ByName -> Strings.settingsThemeSortByName
    ThemeSorting.ByRepo -> Strings.settingsThemeSortByRepo
  }

@Composable
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

@Composable
private fun ThemeFilter.icon(): ImageVector =
  when (this) {
    ThemeFilter.All -> MaterialIcons.ThemeRoutine
    ThemeFilter.Light -> MaterialIcons.LightMode
    ThemeFilter.Dark -> MaterialIcons.DarkMode
  }

@Composable
private fun BottomSheetIcon(imageVector: ImageVector, contentDescription: String? = null) {
  Icon(
    modifier = Modifier.size(24.dp),
    imageVector = imageVector,
    contentDescription = contentDescription,
  )
}
