package aktual.prefs.ui.theme

import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import aktual.prefs.vm.theme.CatalogOptions
import aktual.prefs.vm.theme.DownloadFilter
import aktual.prefs.vm.theme.ThemeFilter
import aktual.prefs.vm.theme.ThemeSorting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun CatalogOptionsSheet(
  options: CatalogOptions,
  onAction: OnAction<ThemeSettingsAction>,
  modifier: Modifier = Modifier,
) {
  AktualModalBottomSheet(
    modifier = modifier,
    onDismissRequest = { onAction(DismissCatalogOptions) },
  ) {
    CatalogOptionsContent(options, onAction)
  }
}

@Composable
private fun CatalogOptionsContent(
  options: CatalogOptions,
  onAction: OnAction<ThemeSettingsAction>,
  modifier: Modifier = Modifier,
) =
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Section(Strings.settingsThemeSort) {
      AktualSlidingToggleButton(
        selected = options.sorting,
        options = SortingOptions,
        onSelect = { onAction(SetCatalogOptions(options.copy(sorting = it))) },
        string = { it.string() },
        fontSize = textSizes.medium,
      )
    }

    Section(Strings.settingsThemeFilter) {
      AktualSlidingToggleButton(
        selected = options.mode,
        options = FilterOptions,
        onSelect = { onAction(SetCatalogOptions(options.copy(mode = it))) },
        string = { it.string() },
        fontSize = textSizes.medium,
      )

      AktualSlidingToggleButton(
        selected = options.download,
        options = DownloadOptions,
        onSelect = { onAction(SetCatalogOptions(options.copy(download = it))) },
        string = { it.string() },
        fontSize = textSizes.medium,
      )
    }
  }

@Composable
private fun Section(title: String, content: @Composable () -> Unit) =
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(text = title, fontSize = textSizes.smallPlus, color = colors.pageTextLight)
    content()
  }

private val SortingOptions = ThemeSorting.entries.toImmutableList()
private val FilterOptions = ThemeFilter.entries.toImmutableList()
private val DownloadOptions = DownloadFilter.entries.toImmutableList()

@Composable
private fun ThemeSorting.string(): String =
  when (this) {
    ByName -> Strings.settingsThemeSortByName
    ByRepo -> Strings.settingsThemeSortByRepo
    DownloadedFirst -> Strings.settingsThemeSortDownloaded
  }

@Composable
private fun ThemeFilter.string(): String =
  when (this) {
    All -> Strings.settingsThemeFilterAll
    Light -> Strings.settingsThemeFilterLight
    Dark -> Strings.settingsThemeFilterDark
  }

@Composable
private fun DownloadFilter.string(): String =
  when (this) {
    AllThemes -> Strings.settingsThemeFilterAll
    DownloadedOnly -> Strings.settingsThemeFilterDownloaded
    NotDownloaded -> Strings.settingsThemeFilterNotDownloaded
  }

@Preview
@Composable
private fun PreviewCatalogOptionsContent(
  @PreviewParameter(CatalogOptionsProvider::class) params: ColorParams<CatalogOptions>,
) = PreviewWithColorParams(params) { CatalogOptionsContent(options = this, onAction = {}) }

private class CatalogOptionsProvider :
  ColorParamsProvider<CatalogOptions>(
    CatalogOptions(),
    CatalogOptions(mode = Dark, download = DownloadedOnly, sorting = DownloadedFirst),
  )
