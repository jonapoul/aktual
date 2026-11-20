package aktual.prefs.ui.search

import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudDownload
import aktual.core.icons.ColorPalette
import aktual.core.icons.Pylon
import aktual.core.icons.SettingsSliderAlternate
import aktual.core.icons.Tuning
import aktual.core.icons.material.Apps
import aktual.core.icons.material.BlurOn
import aktual.core.icons.material.CalendarViewWeek
import aktual.core.icons.material.Dialogs
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Numbers
import aktual.core.icons.material.ReceiptLong
import aktual.core.icons.material.SearchOff
import aktual.core.icons.material.Security
import aktual.core.icons.material.Speed125
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.lineHeights
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens.Large
import aktual.core.ui.Dimens.ListItemSpacing
import aktual.core.ui.Dimens.Medium
import aktual.core.ui.Dimens.ScreenPadding
import aktual.core.ui.Dimens.VeryLarge
import aktual.core.ui.OnAction
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.ScrollToTopOnNewQuery
import aktual.core.ui.SearchMessage
import aktual.core.ui.SearchScaffold
import aktual.core.ui.disabled
import aktual.core.ui.rememberHighlighted
import aktual.core.ui.scrollbar
import aktual.core.ui.searchResultCard
import aktual.prefs.vm.search.BudgetSettingsPages
import aktual.prefs.vm.search.SearchSettingsState
import aktual.prefs.vm.search.SearchSettingsState.Results
import aktual.prefs.vm.search.SearchSettingsViewModel
import aktual.prefs.vm.search.SettingsPage
import aktual.prefs.vm.search.SettingsSearchResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SearchSettingsScreen(
  back: BackNavigator,
  toPage: SettingsPageNavigator,
  viewModel: SearchSettingsViewModel = metroViewModel(),
) {
  val query by viewModel.query.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  SearchSettingsScaffold(
    query = query,
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is SetQuery -> viewModel.setQuery(action.query)
        is OpenSetting -> toPage(action.setting.page, action.setting.highlight())
      }
    },
  )
}

@Composable
private fun SearchSettingsScaffold(
  query: String,
  state: SearchSettingsState,
  onAction: OnAction<SearchSettingsAction>,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  ScrollToTopOnNewQuery(listState, (state as? Results)?.query)

  SearchScaffold(
    modifier = modifier,
    initialQuery = query,
    placeholder = Strings.settingsSearchPlaceholder,
    listState = listState,
    onQueryChange = { onAction(SetQuery(it)) },
    onBack = { onAction(NavBack) },
  ) { innerPadding ->
    AnimatedContent(
      modifier = Modifier.padding(innerPadding),
      targetState = state,
      contentKey = { it::class },
      transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { animatedState ->
      when (animatedState) {
        NoQuery -> SearchMessage(text = Strings.settingsSearchPrompt)

        NoResults ->
          SearchMessage(
            icon = MaterialIcons.SearchOff,
            text = Strings.settingsSearchNoResults(query.trim()),
          )

        is Results ->
          ResultsList(results = animatedState, listState = listState, onAction = onAction)
      }
    }
  }
}

@Composable
private fun ResultsList(
  results: Results,
  listState: LazyListState,
  onAction: OnAction<SearchSettingsAction>,
  modifier: Modifier = Modifier,
) =
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = Medium),
    verticalArrangement = Arrangement.spacedBy(ListItemSpacing),
  ) {
    items(results.settings, key = { it.key }) { setting ->
      ResultItem(
        modifier = Modifier.animateItem(),
        setting = setting,
        query = results.query,
        onClick = { onAction(OpenSetting(setting)) },
      )
    }

    item(key = "bottom") { BottomSpacing() }
  }

@Composable
private fun ResultItem(
  setting: SettingsSearchResult,
  query: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier =
      modifier.searchResultCard(
        colors = colors,
        onClick = onClick,
        padding = PaddingValues(horizontal = VeryLarge, vertical = Large),
        enabled = setting.isEnabled,
      ),
    horizontalArrangement = Arrangement.spacedBy(VeryLarge),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      modifier = Modifier.size(20.dp),
      imageVector = setting.page.icon(),
      contentDescription = null,
      tint = colors.tableTextLight.orDisabled(setting.isEnabled),
    )

    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(
        text = rememberHighlighted(setting.title, query),
        style = typography.bodyMedium,
        color = colors.tableText.orDisabled(setting.isEnabled),
      )

      // Budget settings lead with the budget's name, so they're told apart from this device's
      val breadcrumbs = listOfNotNull(setting.budgetLabel()) + setting.path
      if (breadcrumbs.isNotEmpty()) {
        Text(
          text = breadcrumbs.joinToString(separator = " › "),
          style = typography.bodySmall,
          // Tighter than bodySmall so wrapped breadcrumbs read as one block under the title
          lineHeight = lineHeights.largePlus,
          color = colors.tableTextLight.orDisabled(setting.isEnabled),
        )
      }
    }
  }

@Composable
private fun SettingsSearchResult.budgetLabel(): String? {
  if (page !in BudgetSettingsPages) return null
  val name = budgetName
  return when {
    name == null -> Strings.settingsBudgetGroup
    name.isBlank() -> Strings.settingsBudgetUnnamed
    else -> name
  }
}

@Stable private fun Color.orDisabled(isEnabled: Boolean): Color = if (isEnabled) this else disabled

// path[0] is the page, path[1] the group holding a preference, if it's in one
@Stable
private fun SettingsSearchResult.highlight(): SettingsHighlight? =
  when (kind) {
    Page -> null
    Group -> SettingsHighlight(group = null, title = title)
    Preference -> SettingsHighlight(group = path.getOrNull(1), title = title)
  }

@Stable
private fun SettingsPage.icon(): ImageVector =
  when (this) {
    Theme -> AktualIcons.ColorPalette
    SystemUi -> AktualIcons.Tuning
    SettingsPage.Security -> MaterialIcons.Security
    Format -> MaterialIcons.Numbers
    Amounts -> MaterialIcons.Speed125
    Dates -> MaterialIcons.CalendarViewWeek
    Transactions -> MaterialIcons.ReceiptLong
    Bars -> MaterialIcons.BlurOn
    SettingsPage.Dialogs -> MaterialIcons.Dialogs
    LauncherIcon -> MaterialIcons.Apps
    Updates -> AktualIcons.CloudDownload
    Advanced -> AktualIcons.SettingsSliderAlternate
    Experimental -> AktualIcons.Pylon
  }

private val PreviewSearchResults =
  persistentListOf(
    SettingsSearchResult(
      title = "Blur behind dialogs",
      kind = Preference,
      page = SystemUi,
      path = persistentListOf(SystemUiTitle, DialogsTitle),
    ),
    SettingsSearchResult(
      title = "Blur strength",
      kind = Preference,
      page = SystemUi,
      path = persistentListOf(SystemUiTitle, "App bars"),
    ),
    SettingsSearchResult(
      title = "Blur strength",
      kind = Preference,
      page = SystemUi,
      path = persistentListOf(SystemUiTitle, DialogsTitle),
    ),
    // No budget open, so it's disabled
    SettingsSearchResult(
      title = "First day of the week",
      kind = Preference,
      page = Dates,
      path = persistentListOf("Dates"),
    ),
    SettingsSearchResult(
      title = "Currency Support",
      kind = Preference,
      page = Experimental,
      path = persistentListOf("Main Budget", "Experimental", "Synced with this budget"),
    ),
  )

private class SettingsSearchResultProvider :
  ColorParamsProvider<SettingsSearchResult>(PreviewSearchResults)

@Preview
@Composable
private fun PreviewResultItem(
  @PreviewParameter(SettingsSearchResultProvider::class) params: ColorParams<SettingsSearchResult>,
) =
  PreviewWithColorParams(params) {
    ResultItem(setting = this, query = "", onClick = {})
  }

private data class SearchSettingsParams(val query: String, val state: SearchSettingsState)

private class SearchSettingsScaffoldProvider :
  ColorParamsProvider<SearchSettingsParams>(
    SearchSettingsParams(query = "", state = NoQuery),
    SearchSettingsParams(query = "xyz", state = NoResults),
    SearchSettingsParams(
      query = "blur",
      state = Results(query = "blur", settings = PreviewSearchResults),
    ),
  )

@PortraitPreview
@Composable
private fun PreviewSearchSettingsScaffold(
  @PreviewParameter(SearchSettingsScaffoldProvider::class)
  params: ColorParams<SearchSettingsParams>,
) =
  PreviewWithColorParams(params) {
    SearchSettingsScaffold(query = query, state = state, onAction = {})
  }

private const val SystemUiTitle = "System UI"
private const val DialogsTitle = "Dialogs"
