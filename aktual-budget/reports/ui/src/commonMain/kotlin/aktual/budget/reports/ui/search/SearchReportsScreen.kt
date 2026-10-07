package aktual.budget.reports.ui.search

import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.dashboard.displayName
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.dashboard.DashboardPage
import aktual.budget.reports.vm.search.SearchReportsGroup
import aktual.budget.reports.vm.search.SearchReportsItem
import aktual.budget.reports.vm.search.SearchReportsState
import aktual.budget.reports.vm.search.SearchReportsState.Results
import aktual.budget.reports.vm.search.SearchReportsViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.ReportNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens.Large
import aktual.core.ui.Dimens.Medium
import aktual.core.ui.Dimens.Small
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.ScrollToTopOnNewQuery
import aktual.core.ui.SearchMessage
import aktual.core.ui.SearchScaffold
import aktual.core.ui.rememberHighlighted
import aktual.core.ui.scrollbar
import aktual.core.ui.searchResultCard
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SearchReportsScreen(
  back: BackNavigator,
  toReport: ReportNavigator,
  viewModel: SearchReportsViewModel = metroViewModel(),
) {
  val query by viewModel.query.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  SearchReportsScaffold(
    query = query,
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is SetQuery -> viewModel.setQuery(action.query)
        is OpenReport -> toReport(action.id)
      }
    },
  )
}

@Composable
private fun SearchReportsScaffold(
  query: String,
  state: SearchReportsState,
  onAction: SearchReportsActionHandler,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  ScrollToTopOnNewQuery(listState, (state as? Results)?.query)

  SearchScaffold(
    modifier = modifier,
    initialQuery = query,
    placeholder = Strings.reportsSearchPlaceholder,
    listState = listState,
    onQueryChange = { onAction(SetQuery(it)) },
    onBack = { onAction(NavBack) },
  ) { innerPadding ->
    AnimatedContent(
      targetState = state,
      contentKey = { it::class },
      transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { animatedState ->
      SearchReportsContent(
        query = query,
        state = animatedState,
        innerPadding = innerPadding,
        listState = listState,
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun SearchReportsContent(
  query: String,
  state: SearchReportsState,
  innerPadding: PaddingValues,
  listState: LazyListState,
  onAction: SearchReportsActionHandler,
) =
  when (state) {
    NoQuery ->
      SearchMessage(modifier = Modifier.padding(innerPadding), text = Strings.reportsSearchPrompt)

    NoResults ->
      SearchMessage(
        modifier = Modifier.padding(innerPadding),
        icon = MaterialIcons.SearchOff,
        text = Strings.reportsSearchNoResults(query.trim()),
      )

    is Results ->
      ResultsList(
        // Not under the top bar, since sticky headers ignore content padding and would hide behind
        // it
        modifier = Modifier.padding(innerPadding),
        results = state,
        listState = listState,
        onAction = onAction,
      )
  }

@Composable
private fun ResultsList(
  results: Results,
  listState: LazyListState,
  onAction: SearchReportsActionHandler,
  modifier: Modifier = Modifier,
) =
  LazyColumn(modifier = modifier.fillMaxSize().scrollbar(listState), state = listState) {
    results.groups.forEach { group ->
      stickyHeader(key = "page-${group.page.id.value}") {
        GroupHeader(
          modifier = Modifier.animateItem(),
          page = group.page,
          count = group.items.size,
        )
      }

      items(group.items, key = { it.id.value }) { item ->
        ResultItem(
          modifier = Modifier.animateItem().padding(horizontal = Large, vertical = Medium),
          item = item,
          query = results.query,
          onClick = { onAction(OpenReport(item.id)) },
        )
      }
    }

    item(key = "bottom") { BottomSpacing() }
  }

@Composable
private fun GroupHeader(page: DashboardPage, count: Int, modifier: Modifier = Modifier) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.pageBackground)
        .padding(start = Large + 12.dp, end = Large, top = 12.dp, bottom = Medium),
    horizontalArrangement = Arrangement.spacedBy(Large),
    verticalAlignment = CenterVertically,
  ) {
    Text(text = page.displayName(), color = colors.pageTextSubdued, style = typography.labelLarge)
    Text(text = count.toString(), color = colors.pageTextSubdued, style = typography.labelSmall)
    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.tableBorder)
  }

@Composable
private fun ResultItem(
  item: SearchReportsItem,
  query: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val typeName = item.type.string()
  val subtitle = item.content?.takeIf { it.isNotBlank() } ?: typeName.takeIf { item.name != null }

  Column(
    modifier = modifier.searchResultCard(colors, onClick),
    verticalArrangement = Arrangement.spacedBy(Small),
  ) {
    Text(
      text = rememberHighlighted(item.name?.takeIf { it.isNotBlank() } ?: typeName, query),
      color = colors.tableText,
      style = typography.bodyLarge,
      maxLines = 1,
      overflow = Ellipsis,
    )

    if (subtitle != null) {
      Text(
        text = rememberHighlighted(subtitle, query),
        color = colors.pageTextSubdued,
        style = typography.bodySmall,
        maxLines = 2,
        overflow = Ellipsis,
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewSearchReportsScaffold(
  @PreviewParameter(SearchReportsScaffoldProvider::class) params: ColoredParams<SearchReportsParams>
) =
  PreviewWithColoredParams(params) {
    SearchReportsScaffold(query = query, state = state, onAction = {})
  }

private data class SearchReportsParams(val query: String, val state: SearchReportsState)

private class SearchReportsScaffoldProvider :
  ColoredParameterProvider<SearchReportsParams>(
    SearchReportsParams(query = "", state = NoQuery),
    SearchReportsParams(query = "xyz", state = NoResults),
    SearchReportsParams(
      query = "net",
      state =
        Results(
          query = "net",
          groups =
            persistentListOf(
              SearchReportsGroup(
                page = DashboardPage(DashboardPageId("a"), "Main"),
                items =
                  persistentListOf(
                    SearchReportsItem(WidgetId("1"), NetWorth, name = "Net worth", content = null),
                    SearchReportsItem(
                      WidgetId("2"),
                      CashFlow,
                      name = "Net cash flow",
                      content = null,
                    ),
                  ),
              ),
              SearchReportsGroup(
                page = DashboardPage(DashboardPageId("b"), ""),
                items =
                  persistentListOf(
                    SearchReportsItem(
                      id = WidgetId("3"),
                      type = Markdown,
                      name = null,
                      content = "Keep an eye on net spending each month",
                    )
                  ),
              ),
            ),
        ),
    ),
  )
