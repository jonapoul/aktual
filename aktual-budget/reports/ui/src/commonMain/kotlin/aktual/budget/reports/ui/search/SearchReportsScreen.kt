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
import aktual.core.icons.material.Search
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.ReportNavigator
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens.Large
import aktual.core.ui.Dimens.Medium
import aktual.core.ui.Dimens.Small
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.filter

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
  val keyboard = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current

  // Only for a new query, not when coming back from a report
  val resultsQuery = (state as? Results)?.query
  var lastResultsQuery by rememberSaveable { mutableStateOf(resultsQuery) }
  SideEffect(resultsQuery) {
    if (resultsQuery != null && resultsQuery != lastResultsQuery) {
      lastResultsQuery = resultsQuery
      listState.requestScrollToItem(0)
    }
  }

  LaunchedEffect(listState) {
    snapshotFlow { listState.isScrollInProgress }
      .filter { it }
      .collect {
        keyboard?.hide()
        focusManager.clearFocus()
      }
  }

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(NavBack) }) },
        title = { SearchInput(query, onAction) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
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
      CenteredMessage(
        modifier = Modifier.padding(innerPadding),
        icon = MaterialIcons.Search,
        text = Strings.reportsSearchPrompt,
      )

    NoResults ->
      CenteredMessage(
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
private fun SearchInput(
  initialQuery: String,
  onAction: SearchReportsActionHandler,
  modifier: Modifier = Modifier,
) {
  val state = rememberTextFieldState(initialText = initialQuery)
  val focusRequester = remember { FocusRequester() }
  val keyboard = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current
  var hasFocused by rememberSaveable { mutableStateOf(false) }

  // Only on first open, not when coming back from a report
  LaunchedEffect(Unit) {
    if (!hasFocused) {
      focusRequester.requestFocus()
      hasFocused = true
    }
  }

  LaunchedEffect(state) {
    snapshotFlow { state.text.toString() }.collect { query -> onAction(SetQuery(query)) }
  }

  ProvideTextStyle(value = typography.bodyLarge) {
    AktualTextField(
      modifier = modifier.focusRequester(focusRequester).fillMaxWidth(),
      state = state,
      singleLine = true,
      placeholderText = Strings.reportsSearchPlaceholder,
      showBorder = false,
      clearable = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      onKeyboardAction = {
        keyboard?.hide()
        focusManager.clearFocus()
      },
    )
  }
}

@Composable
private fun CenteredMessage(icon: ImageVector, text: String, modifier: Modifier = Modifier) =
  Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Center) {
    Column(
      horizontalAlignment = CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Large),
    ) {
      Icon(
        modifier = Modifier.size(48.dp),
        imageVector = icon,
        contentDescription = null,
        tint = colors.pageTextSubdued,
      )
      Text(
        text = text,
        color = colors.pageTextSubdued,
        style = typography.bodyLarge,
        textAlign = Center,
      )
    }
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
    modifier =
      modifier
        .fillMaxWidth()
        .clip(CardShape)
        .background(colors.tableBackground)
        .clickable(onClick = onClick)
        .padding(12.dp),
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

@Composable
private fun rememberHighlighted(text: String, query: String): AnnotatedString =
  remember(text, query) {
    buildAnnotatedString {
      append(text)
      if (query.isEmpty()) return@buildAnnotatedString
      var index = text.indexOf(query, ignoreCase = true)
      while (index >= 0) {
        addStyle(SpanStyle(fontWeight = Bold), index, index + query.length)
        index = text.indexOf(query, startIndex = index + query.length, ignoreCase = true)
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
