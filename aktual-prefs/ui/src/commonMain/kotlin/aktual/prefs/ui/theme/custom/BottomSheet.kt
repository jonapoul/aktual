package aktual.prefs.ui.theme.custom

import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.prefs.vm.theme.custom.ThemeFilter
import aktual.prefs.vm.theme.custom.ThemeSorting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.toImmutableList

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
    SortFilterContent(sorting, filter, onAction)
  }
}

@Composable
private fun SortFilterContent(
  sorting: ThemeSorting,
  filter: ThemeFilter,
  onAction: CustomThemeSettingsActionHandler,
  modifier: Modifier = Modifier,
) =
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Section(Strings.settingsThemeSort) {
      AktualSlidingToggleButton(
        selected = sorting,
        options = SortingOptions,
        onSelect = { onAction(SetSorting(it)) },
        string = { it.string() },
        fontSize = 14.sp,
      )
    }

    Section(Strings.settingsThemeFilter) {
      AktualSlidingToggleButton(
        selected = filter,
        options = FilterOptions,
        onSelect = { onAction(SetModeFilter(it)) },
        string = { it.string() },
        fontSize = 14.sp,
      )
    }
  }

@Composable
private fun Section(title: String, content: @Composable () -> Unit) =
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(text = title, fontSize = 13.sp, color = colors.pageTextLight)
    content()
  }

private val SortingOptions = ThemeSorting.entries.toImmutableList()
private val FilterOptions = ThemeFilter.entries.toImmutableList()

@Composable
private fun ThemeSorting.string(): String =
  when (this) {
    ByName -> Strings.settingsThemeSortByName
    ByRepo -> Strings.settingsThemeSortByRepo
  }

@Composable
private fun ThemeFilter.string(): String =
  when (this) {
    All -> Strings.settingsThemeFilterAll
    Light -> Strings.settingsThemeFilterLight
    Dark -> Strings.settingsThemeFilterDark
  }

@Preview
@Composable
private fun PreviewSortFilterContent(
  @PreviewParameter(SortFilterProvider::class) params: ColorParams<SortFilterParams>,
) =
  PreviewWithColorParams(params) {
    SortFilterContent(sorting = sorting, filter = filter, onAction = {})
  }

private data class SortFilterParams(val sorting: ThemeSorting, val filter: ThemeFilter)

private class SortFilterProvider :
  ColorParamsProvider<SortFilterParams>(
    SortFilterParams(ByName, All),
    SortFilterParams(ByRepo, Dark),
  )
