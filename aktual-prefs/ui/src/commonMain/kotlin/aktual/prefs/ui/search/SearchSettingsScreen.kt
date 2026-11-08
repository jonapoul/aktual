package aktual.prefs.ui.search

import aktual.core.icons.AktualIcons
import aktual.core.icons.System
import aktual.core.icons.Tuning
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Numbers
import aktual.core.icons.material.SearchOff
import aktual.core.icons.material.Security
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.FormatSettingsNavigator
import aktual.core.nav.SecuritySettingsNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.nav.SystemUiSettingsNavigator
import aktual.core.nav.ThemeSettingsNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens.Large
import aktual.core.ui.Dimens.ListItemSpacing
import aktual.core.ui.Dimens.Medium
import aktual.core.ui.Dimens.ScreenPadding
import aktual.core.ui.Dimens.VeryLarge
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.ScrollToTopOnNewQuery
import aktual.core.ui.SearchMessage
import aktual.core.ui.SearchScaffold
import aktual.core.ui.rememberHighlighted
import aktual.core.ui.scrollbar
import aktual.core.ui.searchResultCard
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SearchSettingsScreen(
  back: BackNavigator,
  toThemeSettings: ThemeSettingsNavigator,
  toSystemUiSettings: SystemUiSettingsNavigator,
  toSecuritySettings: SecuritySettingsNavigator,
  toFormatSettings: FormatSettingsNavigator,
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
        is OpenSetting -> {
          val highlight = action.setting.highlight()
          when (action.setting.page) {
            Theme -> toThemeSettings(highlight)
            SystemUi -> toSystemUiSettings(highlight)
            SettingsPage.Security -> toSecuritySettings(highlight)
            Format -> toFormatSettings(highlight)
          }
        }
      }
    },
  )
}

@Composable
private fun SearchSettingsScaffold(
  query: String,
  state: SearchSettingsState,
  onAction: SearchSettingsActionHandler,
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
  onAction: SearchSettingsActionHandler,
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
      ),
    horizontalArrangement = Arrangement.spacedBy(VeryLarge),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      modifier = Modifier.size(20.dp),
      imageVector = setting.page.icon(),
      contentDescription = null,
      tint = colors.tableTextLight,
    )

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = rememberHighlighted(setting.title, query),
        style = typography.bodyMedium,
        color = colors.tableText,
      )

      if (setting.path.isNotEmpty()) {
        Text(
          text = setting.path.joinToString(separator = " › "),
          style = typography.bodySmall,
          color = colors.tableTextLight,
        )
      }
    }
  }

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
    Theme -> AktualIcons.System
    SystemUi -> AktualIcons.Tuning
    SettingsPage.Security -> MaterialIcons.Security
    Format -> MaterialIcons.Numbers
  }

@PortraitPreview
@Composable
private fun PreviewSearchSettingsScaffold(
  @PreviewParameter(SearchSettingsScaffoldProvider::class)
  params: ColorParams<SearchSettingsParams>,
) =
  PreviewWithColorParams(params) {
    SearchSettingsScaffold(query = query, state = state, onAction = {})
  }

private data class SearchSettingsParams(val query: String, val state: SearchSettingsState)

private class SearchSettingsScaffoldProvider :
  ColorParamsProvider<SearchSettingsParams>(
    SearchSettingsParams(query = "", state = NoQuery),
    SearchSettingsParams(query = "xyz", state = NoResults),
    SearchSettingsParams(
      query = "blur",
      state =
        Results(
          query = "blur",
          settings =
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
            ),
        ),
    ),
  )

private const val SystemUiTitle = "System UI"
private const val DialogsTitle = "Dialogs"
