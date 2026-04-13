package aktual.prefs.ui.theme.custom

import aktual.core.icons.material.Check
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.listItem
import aktual.core.ui.scrollbar
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.DismissBottomSheet
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.SetModeFilter
import aktual.prefs.vm.theme.custom.ThemeFilter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ThemeFilterBottomSheet(
  selected: ThemeFilter,
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

    val options =
      persistentListOf(
        ThemeFilter.All to Strings.settingsThemeAll,
        ThemeFilter.Light to Strings.settingsThemeLight,
        ThemeFilter.Dark to Strings.settingsThemeDark,
      )

    LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
      items(options) { (filter, label) ->
        val isSelected = filter == selected
        ListItem(
          modifier =
            Modifier.clickable {
              onAction(SetModeFilter(filter))
              onAction(DismissBottomSheet)
            },
          headlineContent = { Text(modifier = Modifier.weight(1f), text = label, textAlign = TextAlign.Center) },
          trailingContent = {
            if (isSelected) Icon(MaterialIcons.Check, contentDescription = null)
          },
          colors = theme.listItem(),
        )
      }
    }
  }
}

internal sealed interface ThemeFilterBottomSheetAction {
  data object Dismiss : ThemeFilterBottomSheetAction

  data class SetValue(val filter: ThemeFilter) : ThemeFilterBottomSheetAction
}
